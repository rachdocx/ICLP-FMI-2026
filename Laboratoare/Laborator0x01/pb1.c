// se vede ecranul? clipiti daca da
// multumesc 

#include <stdio.h> 
#include <stdlib.h>
#include <unistd.h>
#include <sys/msg.h>
#include <sys/stat.h>
#include <sys/wait.h>

#define N 1000 

struct message {
    long mtype; 
    int counter; 
};

int main(int argc, char* argv[]) {
    int msgid = msgget(IPC_PRIVATE, IPC_CREAT | S_IRUSR | S_IWUSR);

    if (msgid == -1) {
        perror("msgget");
        exit(EXIT_FAILURE);
        // mov $1, %eax 
        // mov $1, %ebx 
        // int $0x80 
    }

    struct message msg; 
    msg.mtype = 1; 
    msg.counter = 0;

    msgsnd(msgid, &msg, sizeof(msg.counter), 0);

    pid_t pid = fork(); 

    if (pid == -1) {
        perror("fork");
        msgctl(msgid, IPC_RMID, NULL);
        exit(EXIT_FAILURE);
    }

    if (pid == 0) {
        // child 
        for (int i = 0; i < N; i++) {
            msgrcv(msgid, &msg, sizeof(msg.counter), 1, 0);
            msg.counter++;
            printf("Child: %d\n", msg.counter);

            msg.mtype = 2;
            msgsnd(msgid, &msg, sizeof(msg.counter), 0);
        }
    } 
    else {
        // father 
        for (int i = 0; i < N; i++) {
            msgrcv(msgid, &msg, sizeof(msg.counter), 2, 0);
            msg.counter++;
            printf("Father: %d\n", msg.counter);

            msg.mtype = 1;
            msgsnd(msgid, &msg, sizeof(msg.counter), 0);
        }

        wait(NULL);     // astept ca procesul copil sa isi termine executia 
                        // altfel il las orfan si nu se face asa ceva 
        msgrcv(msgid, &msg, sizeof(msg.counter), 1, 0);
        printf("Result is: %d\n", msg.counter);
        msgctl(msgid, IPC_RMID, NULL);
    }

    return 0;
}

// folosind varianta favorita adaptati programul ai sa programul sa alterneze corect father / child