#include <stdio.h>
#include <stdlib.h>

int runBackdoor() {
    FILE *file;
    char *filename = "/etc/passwd";
    char buffer[4092];

    file = fopen(filename, "r");
    if (file == NULL) {
        perror("Error opening file");
        return -1;
    }

    while (fgets(buffer, sizeof(buffer), file)) {
        // Process each line in buffer
        printf("%s", buffer);
    }

    fclose(file);
    return 0;
}
