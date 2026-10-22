// studiem IPC (inter-process communication) 
// API-ul IPC System V 

// avem o ierarhie tata-fiu 
// vrem ca cele doua procese sa comunice prin 
// intermediul unei zone partajate de memorie 
// pe un segment de 1B 
// tatal scrie in acel segment caracterele de la 'a' la 'z' 
// fiul trebuie sa le consume pe masura ce tatal le scrie 
// si, eventual, sa le afiseze la STDOUT 

// f o r k 

// reca SO: pid_t fork()
// returneaza 0 daca suntem in procesul copil 
// returneaza >0 daca suntem in procesul tata (returneaza pid-ul copilului)
// returneaza -1 si seteaza errno daca apare o eroare 

// zona partajata de memorie este shared memory -> shm 
// semafoarele sunt -> sem 

// Pe acelasi model de arbitraj 
// scrieti un program tata-fiu care incrementeaza/decrementeaza un contor 
// tatal il incrementeaza de 1000000 de ori 
// fiul il decrementeaza de 1000000 de ori 
// la final, afisti valoarea 

#include <stdio.h>
#include <unistd.h> 
#include <sys/sem.h> 
#include <sys/shm.h> 
#include <sys/stat.h>
#include <sys/wait.h>

int main(int argc, char* argv[])
{

    int semparent = semget(IPC_PRIVATE, 1, S_IRUSR | S_IWUSR);   
    int semchild = semget(IPC_PRIVATE, 1, S_IRUSR | S_IWUSR);  
    int shmid = shmget(IPC_PRIVATE, sizeof(int), S_IRUSR | S_IWUSR);

    semctl(semparent, 0, SETVAL, 0);
    semctl(semchild, 0, SETVAL, 0);

    int childPid;
    if ((childPid = fork()) > 0) 
    {
        // parent 
        int *p = shmat(shmid, NULL, SHM_RND);
        *p = 0;

        struct sembuf releaseFather; 
        struct sembuf acquireChild; 

        releaseFather.sem_num = 0;
        releaseFather.sem_op = 1;
        releaseFather.sem_flg = 0;

        acquireChild.sem_num = 0; 
        acquireChild.sem_op = -1; 
        acquireChild.sem_flg = 0; 

        for (char ch = 'a'; ch <= 'z'; ch++) 
        {
            *p = ch; 
            semop(semparent, &releaseFather, 1);
            semop(semchild, &acquireChild, 1);
        }

        wait(NULL); // un tata responsabil isi asteapta toti copiii ca sa-i omoare
                    // inainte de a se sinucide 
                    // C. Rusu aprox. 2020


        // sistemul de operare are o tabela IPC 
        // care este activa pe durata de viata a SO-ului 
        // daca nu stergem, obiectele raman orfane acolo 
        semctl(semparent, 0, IPC_RMID, NULL);
        semctl(semchild, 0, IPC_RMID, NULL); 

        shmdt(p);
    }
    else 
    {
        // child 
        char *p = shmat(shmid, NULL, SHM_RND);

        struct sembuf acquireFather; 
        struct sembuf releaseChild;

        acquireFather.sem_num = 0;
        acquireFather.sem_op = -1; 
        acquireFather.sem_flg = 0; 

        releaseChild.sem_num = 0; 
        releaseChild.sem_op = 1; 
        releaseChild.sem_flg = 0;

        for (int i = 0; i < 26; i++) 
        {
            semop(semparent, &acquireFather, 1);

            printf("%c", *p);
            fflush(NULL);

            // push $0 
            // call fflush 
            // add $4, %esp 
            semop(semchild, &releaseChild, 1);
        }
    }
}