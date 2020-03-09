/* 
 * File:   server.cpp
 * Author: bkrzyzanek
 *
 * Created on January 16, 2020, 12:22 PM
 */

#include <cstdlib>
#include <asm-generic/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <string.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string>
#include <unistd.h>
#include <vector>
#include <iostream>
#include <sys/socket.h>
#include <iterator>
#include <sstream>
#include <fstream>
#include <unordered_map>
#include <algorithm>



#define TRUE 1 
#define FALSE 0 
#define MAX_CLIENTS 30
#define MAX_CONNECTIONS 10
#define PORT 8887

using namespace std;

struct UserAccount
{
    string name;
    string password;
    vector<string> subscribed_subjects;
    string messages_to_send;
};

struct Client
{
    int sd;
    string name;
};

Client clients[MAX_CLIENTS];

unordered_map<string, UserAccount> users;
unordered_map<string, UserAccount>::iterator it;

vector<string> possible_subjects = {"News", "Social media", "Technology", "Animals", "Music", "Film", "Lifestyle", "Sport", "Business", "Science"};

/* >>>>>>>>>>>>>>>>>>>>>>> Wiadomosci zwrotne <<<<<<<<<<<<<<<<<<<<<<<<<<<<<< */
void response(int socket, string message="Wrong request!")
{
    message.append("\r\n");
    //send request message 
    if( send(socket, message.c_str(), message.length(), 0) != message.length() ) 
    { 
        perror("send"); 
    } 
    cout << "client " << socket << ": " << message << endl;
}

/* >>>>>>>>>>>>>>>>>>>>> Podzial wiadomosci na czesci <<<<<<<<<<<<<<<<<<<<<< */
vector<string> split_message(const string& message, char split_by)
{
    vector<string> parts;
    
    string part;
    istringstream partStream(message);
    while (std::getline(partStream, part, split_by))
    {
        parts.push_back(part);
    }
    
    return parts;
}

/* >>>>>>>>>>>>>>>>>>>>>>> Obsluga nowych połaczeń <<<<<<<<<<<<<<<<<<<<<<<<< */
void handle_new_connection(int &new_socket, int &master_socket, sockaddr_in &address, 
        int &addrlen, Client *clients)
{
    string message = "Hello new client!\r\n";
    
    if ((new_socket = accept(master_socket,
            (struct sockaddr *)&address, (socklen_t*)&addrlen))<0) 
    { 
        perror("accept"); 
        exit(EXIT_FAILURE); 
    } 

    //inform user of socket number - used in send and receive commands 
    printf("New connection , socket fd is %d , ip is : %s , port : %ho \n" ,
            new_socket , inet_ntoa(address.sin_addr) , ntohs(address.sin_port)); 

    //send new connection greeting message 
    if( send(new_socket, message.c_str(), message.length(), 0) != message.length() ) 
    { 
        perror("send"); 
    } 

    cout << "Welcome message sent successfully\n"; 

    //add new socket to array of sockets 
    for (int i = 0; i < MAX_CLIENTS; i++) 
    { 
        //if position is empty 
        if( clients[i].sd == 0 ) 
        { 
            clients[i].sd = new_socket; 
            printf("Adding to list of sockets as %d\n" , i); 

            break; 
        } 
    } 
}

/* Odsylanie subskrybowanych wiadomosci dla uzytkownika, ktory byl nieobecny */
void send_waiting_messages(int socket, string name)
{
    string all_messages = "<";
    it = users.find(name);
    if (it != users.end())
    {
        all_messages.append((*it).second.messages_to_send);
        response(socket, all_messages);
    }
}

/* >>>>>>>>>>>>>>>>>>>>>>>>> Obsluga logowania <<<<<<<<<<<<<<<<<<<<<<<<<<<<< */
void handle_login(Client &client, string message)
{
    vector<string> credentials = split_message(message, ';');
    
    // Sprawdz czy zostal podany login i haslo
    if (credentials.size() == 2)
    {
        // sprawdz czy klient na tym deskryptorze nie jest juz zalogowany
        if (client.name.empty())
        {
            // sprawdz czy istnieje klient o tej nazwie w bazie
            it = users.find(credentials.at(0));
            if (it != users.end())
            {
                // sprawd czy uzytkownik nie jest juz zalogowany na innym kliencie
                bool already_logged = FALSE;
                for (int a=0; a<MAX_CLIENTS; a++)
                    if (clients[a].name == credentials.at(0))
                    {
                        already_logged = TRUE;
                        break;
                    }
                
                if (!already_logged)
                {
                    // sprawdz czy haslo sie zgadza
                    if ((*it).second.password == credentials.at(1))
                    {
                        client.name = credentials.at(0);
                        
                        response(client.sd, "Logged successfully!");
                        send_waiting_messages(client.sd, client.name);
                    }
                    else
                    {
                        response(client.sd, "Wrong password!");
                    }
                }
                else
                {
                    response(client.sd, "You can use only one client in the same time!");
                }
            }
            // Jezeli nie to go zarejestruj
            else
            {
                UserAccount new_user;
                new_user.name = credentials.at(0);
                new_user.password = credentials.at(1);
                new_user.subscribed_subjects = {};
                new_user.messages_to_send = "";
                
                users.insert({credentials.at(0), new_user});
                client.name = credentials.at(0);
                
                response(client.sd, "Registered successfully!");
            }   
        }
        else
        {
            response(client.sd, "You are already logged in!");
        }
    }
    else
    {
        response(client.sd);
    }
    
    credentials.clear();
}

/* >>>>>>>>>>>>>>>>>>>>>>>>>>>> Subskrybcje <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< */
void subscribe_handling(const Client &client, string message)
{
    vector<string> subjects = split_message(message, ';');
    
    // sprawdz czy uzytkownik jest zalogowany
    it = users.find(client.name);
    if (it != users.end())
    {
        // sprawdz czy takie tematy istnieja
        sort(subjects.begin(), subjects.end());
        if(includes(possible_subjects.begin(), possible_subjects.end(), subjects.begin(), subjects.end()))
        {
            (*it).second.subscribed_subjects = subjects;
            response(client.sd, message);
        }
        else
        {
            response(client.sd, "Some subjects does not exist!");
        }
    }
    else
    {
        response(client.sd, "You are not logged in!");
    }   
    
    subjects.clear();
}

/* >>>>>>>>>>>>>>>>>> Odbieranie i wysyłanie wiadomosci <<<<<<<<<<<<<<<<<<<< */
void message_handling(const Client &publisher, string message)
{
        // sprawdz czy uzytkownik jest zalogowany
    it = users.find(publisher.name);
    if (it != users.end())
    {
        // sprawdz czy wiadomosc ma odpowiedni format
        vector<string> msg = split_message(message, ';');
        if (msg.size() == 2)
        {
            // sprawdz czy temat istnieje
            if (count(possible_subjects.begin(), possible_subjects.end(), msg.at(0)))
            {
                // sprawdz co zrobic z wiadomoscia dla kazdego uzytkownika
                for (auto &element : users)
                {
                    // Jeżeli użytkownik subskrybuje rozsyłany temat to wyslij do niego wiadomosc
                    if (element.second.name != publisher.name && count(element.second.subscribed_subjects.begin(), element.second.subscribed_subjects.end(), msg.at(0)))
                    {
                        // Jezeli jest zalogowany wyslij od razu
                        bool found = FALSE;
                        for (int index = 0; index < MAX_CLIENTS; index++)
                        {
                            if (clients[index].name == element.second.name)
                            {
                                string result = ">";
                                result.append(msg.at(1));
                                response(clients[index].sd, result);
                                found = TRUE;
                                break;
                            }
                        }

                        if (!found)
                        {
                            element.second.messages_to_send.append(message);
                        }
                    }
                }
                // poinformuj o pomyslnym rozeslaniu wiadomosci
                response(publisher.sd, message);
            }
            else
            {
                response(publisher.sd, "Subject does not exist!");
            }
        }
        else
        {
            response(publisher.sd);
        }
        
        msg.clear();
    }
    else
    {
        response(publisher.sd, "You are not logged in!");
    } 
}

void write(const string& file_name, UserAccount& data)
{
  ofstream out(file_name.c_str());
  out.write(reinterpret_cast<char*>(&data), sizeof(UserAccount));
}

void read_file()
{
    users.clear();
    
    ifstream in("database.txt");
    
    if (!in.fail())
    {
        string content((istreambuf_iterator<char>(in)), (std::istreambuf_iterator<char>()));
    
        vector<string> users_as_string = split_message(content, '{');

        for (string user_as_string : users_as_string)
        {
            vector<string> user_fields = split_message(user_as_string, '|');
            if (user_fields.size() == 4)
            {
                UserAccount user_account;
                user_account.name = user_fields.at(0);
                user_account.password = user_fields.at(1);
                user_account.messages_to_send = user_fields.at(2);
                user_account.subscribed_subjects = split_message(user_fields.at(3), ';');

                users.insert({user_account.name, user_account});
            }
        }
    }
}

string vector_to_string(vector<string> vec)
{
    string result = "";
    for (string text : vec)
    {
        result.append(text);
        result.append(";");
    }
    
    if (result.size() > 1)
    {
        result.erase(result.size() - 1);  // ;
    }
    
    return result;
}

void save_changes()
{
    ofstream out("database.txt");
    
    for (pair<string, UserAccount> user : users)
    {
        out << "{";
        out << user.second.name;
        out << "|";
        out << user.second.password;
        out << "|";
        out << user.second.messages_to_send;
        out << "|";
        out << vector_to_string(user.second.subscribed_subjects);
    }
}

/*
 * 
 */
int main(int argc, char** argv) 
{
    // sortowanie wszystkich tematow, w celu szybszego przeszukiwania 
    // i porownywania z tymi ktore przesyla klient
    sort(possible_subjects.begin(), possible_subjects.end());
    
    // wczytanie 'bazy danych'
    read_file();
    
    int opt = TRUE; 
    int master_socket, addrlen, new_socket, activity, i, valread, sd; 
    int max_sd; 
    struct sockaddr_in address;
    
    struct timeval
    {
        int tv_sec;
        int tv_usec;
    };

    // zbior deskryptorow socketow
    fd_set readfds; 
    
    // inicjalizacja wszystkich klientow jako niepolaczonych i niezalogowanych
    for (i = 0; i < MAX_CLIENTS; i++) 
    { 
            clients[i].sd = 0;
            clients[i].name = "";
    } 

    // Inicjalizacja glownego socketu
    if( (master_socket = socket(AF_INET , SOCK_STREAM , 0)) == 0) 
    { 
            perror("master socket initialization failed"); 
            exit(EXIT_FAILURE); 
    } 

    // Umozliwienie wielu polaczen do glownego socketu
    if(setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&opt, sizeof(opt)) < 0) 
    { 
            perror("setsockopt failed"); 
            exit(EXIT_FAILURE); 
    } 

    // typ socketu
    address.sin_family = AF_INET; 
    address.sin_addr.s_addr = INADDR_ANY; 
    address.sin_port = htons( PORT ); 

    // bind na localhost
    if (bind(master_socket, (struct sockaddr *)&address, sizeof(address))<0) 
    { 
            perror("bind failed"); 
            exit(EXIT_FAILURE); 
    } 
    printf("Server started on port %d \n", PORT); 

    // maksymalnie MAX_CONNECTIONS czekających na połączenie w kolejce do głównego socketa
    if (listen(master_socket, MAX_CONNECTIONS) < 0) 
    { 
            perror("listen failed"); 
            exit(EXIT_FAILURE); 
    } 

    // Zaakceptuj przychodzące połączenia
    addrlen = sizeof(address); 
    puts("Waiting for connections ...");
   

    // GŁÓWNA PĘTLA PROGRAMU
    while(TRUE) 
    { 
        // Wyzeruj zbiór socketów
        FD_ZERO(&readfds); 

        // Dodaj głównego socketa do zbioru
        FD_SET(master_socket, &readfds); 
        max_sd = master_socket; 
        
        // Dodawanie klientów do zbioru
        for ( i = 0 ; i < MAX_CLIENTS ; i++) 
        { 
            sd = clients[i].sd; 

            // jeżeli jest to poprawnie połączony klient to dodaj go do zbioru 
            if(sd > 0) 
                FD_SET( sd , &readfds); 

            // deskryptor ostatniego klienta - uzywany w funkcji select
            if(sd > max_sd) 
                max_sd = sd; 
        } 

        // oczekiwanie na aktywnosc na jednym z socketow
        activity = select( max_sd + 1 , &readfds , NULL , NULL , NULL); 

        if ((activity < 0) && (errno!=EINTR)) 
        { 
               printf("select error"); 
        } 

        // Jesli zdarzylo sie cos na głownym sockecie
        // jest to nowe polaczenie od klienta
        if (FD_ISSET(master_socket, &readfds)) 
        { 
            handle_new_connection(new_socket, master_socket, address, addrlen, clients);
        } 

        // W przeciwnym wypadku aktywnosc pochodzi od polaczonego klienta
        for (i = 0; i < MAX_CLIENTS; i++) 
        { 
            sd = clients[i].sd; 

            if (FD_ISSET( sd , &readfds)) 
            { 
                // utworz bufor na odczytywane dane
                const unsigned int MAX_BUF_LENGTH = 4096;
                vector<char> buffer(MAX_BUF_LENGTH);
                string message;   
                int bytesReceived = 0;
                // Odczytuj cala wiadomosc 
                do {
                    bytesReceived = recv(sd, &buffer[0], buffer.size(), 0);
                    // jesli wystapil blad to ktos sie rozlaczyl
                    if (bytesReceived <= 0) 
                    { 
                        // Wypisz kto sie rozlaczyk
                        getpeername(sd , (struct sockaddr*)&address , (socklen_t*)&addrlen);   
                        printf("Disconnected: ip %s , port %ho \n", inet_ntoa(address.sin_addr) , ntohs(address.sin_port));   

                        // Zakoncz polaczenie i wyloguj uzytkownika
                        close( sd );   
                        clients[i].sd = 0;
                        clients[i].name = "";
                        break;
                    } 
                    else 
                    {
                        // dodaj odczytana czesc wiadomosci do calkowitej wiadomosci
                        message.append( buffer.cbegin(), buffer.cbegin() + bytesReceived );
                    }
                } while ( bytesReceived == MAX_BUF_LENGTH ); 
                
                // Jezeli wiadomosc zawiera wiecej informacji niz typ wiadomosci 
                // i znaki konca linii, to usun znaki konca linii i podejmij odpowiednie dzialanie
                if (message.size() > 3)
                {
                    char msg_type = message.at(0);
                    message.erase(message.begin()); // Typ
                    message.erase(message.size() - 1); // \n
                    message.erase(message.size() - 1); // \r

                    // Wybor odpowiedniego dzialania na podstawie typu wiadomosci
                    switch (msg_type)
                    {
                        case 'L':
                            handle_login(clients[i], message);
                            break;
                        case 'R':
                            subscribe_handling(clients[i], message);
                            break;
                        case 'W':
                            message_handling(clients[i], message);
                            break;
                        default:
                            response(sd);
                            break;
                    }
                }
            } 
        } 
        
        save_changes();
    } 
		
    shutdown(master_socket, SHUT_RDWR);
    return 0; 
}
