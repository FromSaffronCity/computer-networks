#include<stdio.h>
#include<stdlib.h>
#include<string.h>

/* ******************************************************************
 ALTERNATING BIT AND GO-BACK-N NETWORK EMULATOR: SLIGHTLY MODIFIED
 FROM VERSION 1.1 of J.F.Kurose

   This code should be used for PA2, unidirectional or bidirectional
   data transfer protocols (from A to B. Bidirectional transfer of data
   is for extra credit and is not required).  Network properties:
   - one way network delay averages five time units (longer if there
       are other messages in the channel for GBN), but can be larger
   - packets can be corrupted (either the header or the data portion)
       or lost, according to user-defined probabilities
   - packets will be delivered in the order in which they were sent
       (although some can be lost).
**********************************************************************/

#define BIDIRECTIONAL 1  /* change to 1 if you're doing extra credit */
/* and write a routine called B_output */

/* a "msg" is the data unit passed from layer 5 (teachers code) to layer  */
/* 4 (students' code).  It contains the data (characters) to be delivered */
/* to layer 5 via the students transport level protocol entities. */
struct msg {
    char data[20];
};

/* a packet is the data unit passed from layer 3 (teachers code) to layer 2 (students code). */
/*  It needs to be delivered to layer 3 via the students datalink level protocol entities */
/* Note the pre-defined packet structure, which all students must follow. */
struct pkt {
    int seqnum;
    int acknum;
    int checksum;
    char payload[4];
};

/* type of frame (data/ack) */
#define DATAFRAME 0
#define ACKFRAME 1
#define PIGGYBACKEDACK 2

/* a frame is the data unit passed from layer 2 (students code) to layer */
/* 1 (teachers code).  Note the pre-defined frame structure, which all */
/* students must follow. */
struct frm {
    int type;
    int seqnum;
    int acknum;
    int checksum;
    char payload[4];
};

/********* FUNCTION PROTOTYPES. DEFINED IN THE LATER PART ******************/
void starttimer(int AorB, float increment);
void stoptimer(int AorB);
void tolayer1(int AorB, struct frm frame);
void tolayer3(int AorB, struct pkt packet);

/********* STUDENTS WRITE THE NEXT SEVEN ROUTINES *********/

/* ENTITY-A: 0 & ENTITY-B: 1 */
#define ENTITYA 0
#define ENTITYB 1

/* state variables */
int current_seqnum_A;
int seqnum_expected_A;
int current_acknum_A;
int acknum_expected_A;
int is_any_packet_in_transit_A;
int current_seqnum_B;
int seqnum_expected_B;
int current_acknum_B;
int acknum_expected_B;
int is_any_packet_in_transit_B;

/* shared information */
struct frm frame_to_transmit_A;
struct frm frame_to_transmit_B;

/* generator polynomial G(x): agreed upon by both sender & receiver */
int generator_polynomial;

/* CRC Steps: 0 -> do not show steps of CRC & 1 -> show steps of CRC */
int crc_steps;

/* Piggybacking: 0 -> no piggybacked acknowledgement & 1 -> piggybacked acknowledgement */
int piggybacking;

/* for calculating checksum for given packet */
int calculate_checksum(struct pkt packet) {
    int checksum=packet.seqnum+packet.acknum, i;
    for(i=0; i<20; i++) checksum += (int) packet.payload[i];
    return checksum;
}

/* for calculating degree for given binary string */
int calculate_degree(int binary_string) {
    int degree=0;
    while(binary_string > 1) {
        degree++;
        binary_string /= 10;
    }
    return degree;
}

/* for calculating xor for given two bits */
char do_xor(char x, char y) {
    return (x==y? '0': '1');
}

/* for calculating crc remainder for given frame */
int calculate_cyclic_redundancy_check_remainder(int is_remainder_set, struct frm frame) {
    /* is_remainder_set: 0 -> set crc remainder & 1 -> check crc remainder */
    int gen_pol_degree=calculate_degree(generator_polynomial), decimal_string, binary_string, i, j, starting_index, operand_length=0, shift;
    char appended_frame[128+gen_pol_degree], gen_pol[gen_pol_degree+1], operand[gen_pol_degree+1];

    decimal_string = frame.type;
    for(i=31; i>=0; i--) {
        appended_frame[i] = (char) (decimal_string%2)+48;
        decimal_string /= 2;
    }
    decimal_string = frame.seqnum;
    for(i=63; i>=32; i--) {
        appended_frame[i] = (char) (decimal_string%2)+48;
        decimal_string /= 2;
    }
    decimal_string = frame.acknum;
    for(i=95; i>=64; i--) {
        appended_frame[i] = (char) (decimal_string%2)+48;
        decimal_string /= 2;
    }
    for(i=0; i<4; i++) {
        decimal_string = (int) frame.payload[i];
        for(j=103+i*8; j>=96+i*8; j--) {
            appended_frame[j] = (char) (decimal_string%2)+48;
            decimal_string /= 2;
        }
    }
    for(i=128; i<128+gen_pol_degree; i++) {
        appended_frame[i] = '0';
    }
    binary_string = generator_polynomial;
    for(i=gen_pol_degree; i>=0; i--) {
        gen_pol[i] = (char) (binary_string%10)+48;
        binary_string /= 10;
    }
    if(crc_steps == 1) {
        printf("crc_algorithm: input bit string = ");
        for(i=0; i<128; i++) {
            printf("%c", appended_frame[i]);
        }
        printf("\n");
        printf("crc_algorithm: generator polynomial = ");
        for(i=0; i<gen_pol_degree+1; i++) {
            printf("%c", gen_pol[i]);
        }
        printf("\n");
    }
    
    if(is_remainder_set == 1) {
        binary_string = frame.checksum;
        for(i=gen_pol_degree; i>=0; i--) {
            operand[i] = (char) (binary_string%10)+48;
            binary_string /= 10;
        }
        if(crc_steps == 1) {
            printf(">> crc_algorithm: crc remainder = ");
            for(i=0; i<gen_pol_degree+1; i++) {
                printf("%c", operand[i]);
            }
            printf("\n");
        }

        for(i=gen_pol_degree; i>=0; i--) {
            appended_frame[127+i] = do_xor(appended_frame[127+i], operand[i]);
        }
        if(crc_steps == 1) {
            printf(">> crc_algorithm: transferred frame = ");
            for(i=0; i<128+gen_pol_degree; i++) {
                printf("%c", appended_frame[i]);
            }
            printf("\n");
        }
    }

    for(i=0; i<128+gen_pol_degree; i++) {
        if(appended_frame[i] == '1') {
            starting_index = i;
            break;
        }
    }
    for(i=0; i<gen_pol_degree+1; i++) {
        operand[i] = '-';
    }
    for(i=starting_index; i<128+gen_pol_degree; i++) {
        if(operand_length < gen_pol_degree+1) {
            operand[operand_length++] = appended_frame[i];
        }
        if(operand_length < gen_pol_degree+1) {
            continue;
        } else {
            if(crc_steps == 1) {
                printf(">> crc_algorithm: ");
                for(j=0; j<gen_pol_degree+1; j++) {
                    printf("%c", operand[j]);
                }
                printf(" XOR ");
                for(j=0; j<gen_pol_degree+1; j++) {
                    printf("%c", gen_pol[j]);
                }
            }
        }

        for(j=0; j<gen_pol_degree+1; j++) {
            operand[j] = do_xor(operand[j], gen_pol[j]);
        }
        if(crc_steps == 1) {
            printf(" = ");
            for(j=0; j<gen_pol_degree+1; j++) {
                printf("%c", operand[j]);
            }
            printf("\n");
        }
        shift = gen_pol_degree+1;
        for(j=0; j<gen_pol_degree+1; j++) {
            if(operand[j] == '1') {
                shift = j;
                break;
            }
        }
        for(j=0; j<gen_pol_degree+1-shift; j++) {
            operand[j] = operand[j+shift];
        }
        for(j=gen_pol_degree+1-shift; j<gen_pol_degree+1; j++) {
            operand[j] = '-';
        }
        operand_length -= shift;
    }
    shift = 0;
    for(i=gen_pol_degree; i>=0; i--) {
        if(operand[i] == '-') {
            shift++;
        } else {
            break;
        }
    }
    for(i=gen_pol_degree; i>=0; i--) {
        if(i < shift) {
            operand[i] = '0';
        } else {
            operand[i] = operand[i-shift];
        }
    }

    starting_index = 0;  /* as crc remainder */
    operand_length = 1;  /* as factor */
    for(i=gen_pol_degree; i>=0; i--) {
        starting_index += (int) (operand[i]-48)*operand_length;
        operand_length *= 10;
    }
    if(crc_steps == 1) {
        printf("crc_algorithm: crc remainder = %d\n", starting_index);
    }
    return starting_index;  /* as crc remainder */
}

/* for setting crc remainder for given frame */
int crc_set(struct frm frame) {
    return calculate_cyclic_redundancy_check_remainder(0, frame);
}

/* for checking crc remainder for given frame */
int crc_check(struct frm frame) {
    return calculate_cyclic_redundancy_check_remainder(1, frame);
}

/* called from layer 3, passed the packet to be sent to other side */
void A_output(struct pkt packet) {
    printf("A_output: packet received from layer3.\n");
    if(is_any_packet_in_transit_A == 1) {
        printf("A_output: another packet in transit. dropping this packet.\n");
        return ;
    }
    int i;

    frame_to_transmit_A.seqnum = current_seqnum_A;
    if(seqnum_expected_A != current_acknum_A) {
        frame_to_transmit_A.type = PIGGYBACKEDACK;
        frame_to_transmit_A.acknum = current_acknum_A;
        current_acknum_A = (current_acknum_A==0? 1: 0);
    } else {
        frame_to_transmit_A.type = DATAFRAME;
        frame_to_transmit_A.acknum = 2;  /* don't care */
    }
    for(i=0; i<4; i++) frame_to_transmit_A.payload[i] = packet.payload[i];
    frame_to_transmit_A.checksum = crc_set(frame_to_transmit_A);

    is_any_packet_in_transit_A = 1;
    starttimer(ENTITYA, 15.0);

    if(frame_to_transmit_A.type == DATAFRAME) {
        printf("A_output: data frame with seqnum%d passed to layer1.\n", frame_to_transmit_A.seqnum);
    }
    if(frame_to_transmit_A.type == PIGGYBACKEDACK) {
        printf("A_output: piggybacked ack frame with seqnum%d and acknum%d passed to layer1.\n", frame_to_transmit_A.seqnum, frame_to_transmit_A.acknum);
    }
    tolayer1(ENTITYA, frame_to_transmit_A);
}

/* called from layer 3, passed the packet to be sent to other side */
void B_output(struct pkt packet) {
    printf("B_output: packet received from layer3.\n");
    if(is_any_packet_in_transit_B == 1) {
        printf("B_output: another packet in transit. dropping this packet.\n");
        return ;
    }
    int i;

    frame_to_transmit_B.seqnum = current_seqnum_B;
    if(seqnum_expected_B != current_acknum_B) {
        frame_to_transmit_B.type = PIGGYBACKEDACK;
        frame_to_transmit_B.acknum = current_acknum_B;
        current_acknum_B = (current_acknum_B==0? 1: 0);
    } else {
        frame_to_transmit_B.type = DATAFRAME;
        frame_to_transmit_B.acknum = 2;  /* don't care */
    }
    for(i=0; i<4; i++) frame_to_transmit_B.payload[i] = packet.payload[i];
    frame_to_transmit_B.checksum = crc_set(frame_to_transmit_B);

    is_any_packet_in_transit_B = 1;
    starttimer(ENTITYB, 15.0);

    if(frame_to_transmit_B.type == DATAFRAME) {
        printf("B_output: data frame with seqnum%d passed to layer1.\n", frame_to_transmit_B.seqnum);
    }
    if(frame_to_transmit_B.type == PIGGYBACKEDACK) {
        printf("B_output: piggybacked ack frame with seqnum%d and acknum%d passed to layer1.\n", frame_to_transmit_B.seqnum, frame_to_transmit_B.acknum);
    }
    tolayer1(ENTITYB, frame_to_transmit_B);
}

/* called from layer 1, when a frame arrives for layer 2 at A */
void A_input(struct frm frame) {
    printf("A_input: frame received from layer1.\n");
    if(crc_check(frame) != 0) {
        printf("A_input: frame corrupted. frame dropped.\n");
        return ;
    }
    if(frame.type == DATAFRAME) {
        printf("A_input: data frame with seqnum%d accepted.\n", frame.seqnum);
    } else if(frame.type == ACKFRAME) {
        printf("A_input: ack frame with acknum%d accepted.\n", frame.acknum);
    } else if(frame.type = PIGGYBACKEDACK) {
        printf("A_input: piggybacked ack frame with seqnum%d and acknum%d accepted.\n", frame.seqnum, frame.acknum);
    }
    int i;
    
    if(frame.type==DATAFRAME || frame.type==PIGGYBACKEDACK) {
        if(seqnum_expected_A == frame.seqnum) {
            struct pkt packet_to_layer3;
            packet_to_layer3.seqnum = frame.seqnum;
            packet_to_layer3.acknum = frame.acknum;
            packet_to_layer3.checksum = frame.checksum;
            for(i=0; i<4; i++) packet_to_layer3.payload[i] = frame.payload[i];

            printf("A_input: seqnum matched. packet passed to layer3.\n");
            tolayer3(ENTITYA, packet_to_layer3);
        } else {
            printf("A_input: seqnum mismatched. duplicate frame received.\n");
        }

        if(piggybacking==1 && seqnum_expected_A==frame.seqnum) {
            seqnum_expected_A = (seqnum_expected_A==0? 1: 0);
            printf("A_input: waiting to send ack frame to B.\n");
        } else {
            struct frm ack_frame;
            ack_frame.type = ACKFRAME;
            ack_frame.seqnum = 2;  /* don't care */
            ack_frame.acknum = (seqnum_expected_A==frame.seqnum? current_acknum_A: (seqnum_expected_A+1)%2);
            for(i=0; i<4; i++) ack_frame.payload[i] = '-';
            ack_frame.checksum = crc_set(ack_frame);

            if(piggybacking==1 && seqnum_expected_A!=current_acknum_A) {
                current_acknum_A = (current_acknum_A==0? 1: 0);
            }
            if(piggybacking==0 && seqnum_expected_A==frame.seqnum) {
                seqnum_expected_A = current_acknum_A = (seqnum_expected_A==0? 1: 0);
            }

            printf("A_input: ack frame with acknum%d passed to layer1.\n", ack_frame.acknum);
            tolayer1(ENTITYA, ack_frame);
        }
    } 
    if(frame.type==ACKFRAME || frame.type==PIGGYBACKEDACK) {
        if(acknum_expected_A == frame.acknum) {
            current_seqnum_A = acknum_expected_A = (current_seqnum_A==0? 1: 0);
            is_any_packet_in_transit_A = 0;
            stoptimer(ENTITYA);
        } else {
            printf("A_input: acknum mismatched. ack frame ignored.\n");
        }
    }
}

/* called when A's timer goes off */
void A_timerinterrupt(void) {
    printf("A_timerinterrupt: A's timer went off. retransmitting frame.\n");

    if(frame_to_transmit_A.type == PIGGYBACKEDACK) {
        frame_to_transmit_A.type = DATAFRAME;
        frame_to_transmit_A.acknum = 2;  /* don't care */
    }
    if(seqnum_expected_A != current_acknum_A) {
        frame_to_transmit_A.type = PIGGYBACKEDACK;
        frame_to_transmit_A.acknum = current_acknum_A;
        current_acknum_A = (current_acknum_A==0? 1: 0);
    }
    frame_to_transmit_A.checksum = crc_set(frame_to_transmit_A);
    starttimer(ENTITYA, 15.0);

    if(frame_to_transmit_A.type == DATAFRAME) {
        printf("A_timerinterrupt: data frame with seqnum%d passed to layer1.\n", frame_to_transmit_A.seqnum);
    }
    if(frame_to_transmit_A.type == PIGGYBACKEDACK) {
        printf("A_timerinterrupt: piggybacked ack frame with seqnum%d and acknum%d passed to layer1.\n", frame_to_transmit_A.seqnum, frame_to_transmit_A.acknum);
    }
    tolayer1(ENTITYA, frame_to_transmit_A);
}

/* the following routine will be called once (only) before any other */
/* entity A routines are called. You can use it to do any initialization */
void A_init(void) {
    printf("A_init: initialization for entity A.\n");
    current_seqnum_A = seqnum_expected_A = current_acknum_A = acknum_expected_A = is_any_packet_in_transit_A = 0;
}

/* called from layer 1, when a frame arrives for layer 2 at B */
void B_input(struct frm frame) {
    printf("B_input: frame received from layer1.\n");
    if(crc_check(frame) != 0) {
        printf("B_input: frame corrupted. frame dropped.\n");
        return ;
    }
    if(frame.type == DATAFRAME) {
        printf("B_input: data frame with seqnum%d accepted.\n", frame.seqnum);
    } else if(frame.type == ACKFRAME) {
        printf("B_input: ack frame with acknum%d accepted.\n", frame.acknum);
    } else if(frame.type = PIGGYBACKEDACK) {
        printf("B_input: piggybacked ack frame with seqnum%d and acknum%d accepted.\n", frame.seqnum, frame.acknum);
    }
    int i;
    
    if(frame.type==DATAFRAME || frame.type==PIGGYBACKEDACK) {
        if(seqnum_expected_B == frame.seqnum) {
            struct pkt packet_to_layer3;
            packet_to_layer3.seqnum = frame.seqnum;
            packet_to_layer3.acknum = frame.acknum;
            packet_to_layer3.checksum = frame.checksum;
            for(i=0; i<4; i++) packet_to_layer3.payload[i] = frame.payload[i];

            printf("B_input: seqnum matched. packet passed to layer3.\n");
            tolayer3(ENTITYB, packet_to_layer3);
        } else {
            printf("B_input: seqnum mismatched. duplicate frame received.\n");
        }

        if(piggybacking==1 && seqnum_expected_B==frame.seqnum) {
            seqnum_expected_B = (seqnum_expected_B==0? 1: 0);
            printf("B_input: waiting to send ack frame to A.\n");
        } else {
            struct frm ack_frame;
            ack_frame.type = ACKFRAME;
            ack_frame.seqnum = 2;  /* don't care */
            ack_frame.acknum = (seqnum_expected_B==frame.seqnum? current_acknum_B: (seqnum_expected_B+1)%2);
            for(i=0; i<4; i++) ack_frame.payload[i] = '-';
            ack_frame.checksum = crc_set(ack_frame);

            if(piggybacking==1 && seqnum_expected_B!=current_acknum_B) {
                current_acknum_B = (current_acknum_B==0? 1: 0);
            }
            if(piggybacking==0 && seqnum_expected_B==frame.seqnum) {
                seqnum_expected_B = current_acknum_B = (seqnum_expected_B==0? 1: 0);
            }

            printf("B_input: ack frame with acknum%d passed to layer1.\n", ack_frame.acknum);
            tolayer1(ENTITYB, ack_frame);
        }
    } 
    if(frame.type==ACKFRAME || frame.type==PIGGYBACKEDACK) {
        if(acknum_expected_B == frame.acknum) {
            current_seqnum_B = acknum_expected_B = (current_seqnum_B==0? 1: 0);
            is_any_packet_in_transit_B = 0;
            stoptimer(ENTITYB);
        } else {
            printf("B_input: acknum mismatched. ack frame ignored.\n");
        }
    }
}

/* called when B's timer goes off */
void B_timerinterrupt(void) {
    printf("B_timerinterrupt: B's timer went off. retransmitting frame.\n");

    if(frame_to_transmit_B.type == PIGGYBACKEDACK) {
        frame_to_transmit_B.type = DATAFRAME;
        frame_to_transmit_B.acknum = 2;  /* don't care */
    }
    if(seqnum_expected_B != current_acknum_B) {
        frame_to_transmit_B.type = PIGGYBACKEDACK;
        frame_to_transmit_B.acknum = current_acknum_B;
        current_acknum_B = (current_acknum_B==0? 1: 0);
    }
    frame_to_transmit_B.checksum = crc_set(frame_to_transmit_B);
    starttimer(ENTITYB, 15.0);

    if(frame_to_transmit_B.type == DATAFRAME) {
        printf("B_timerinterrupt: data frame with seqnum%d passed to layer1.\n", frame_to_transmit_B.seqnum);
    }
    if(frame_to_transmit_B.type == PIGGYBACKEDACK) {
        printf("B_timerinterrupt: piggybacked ack frame with seqnum%d and acknum%d passed to layer1.\n", frame_to_transmit_B.seqnum, frame_to_transmit_B.acknum);
    }
    tolayer1(ENTITYB, frame_to_transmit_B);
}

/* the following routine will be called once (only) before any other */
/* entity B routines are called. You can use it to do any initialization */
void B_init(void) {
    printf("B_init: initialization for entity B.\n");
    current_seqnum_B = seqnum_expected_B = current_acknum_B = acknum_expected_B = is_any_packet_in_transit_B = 0;
}

/*****************************************************************
***************** NETWORK EMULATION CODE STARTS BELOW ***********
The code below emulates the layer 3 and below network environment:
    - emulates the tranmission and delivery (possibly with bit-level corruption
        and packet loss) of packets across the layer 3/4 interface
    - handles the starting/stopping of a timer, and generates timer
        interrupts (resulting in calling students timer handler)
    - generates message to be sent (passed from layer 5 to 4).

THERE IS NO REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW. If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you should have
to, and you defeinitely should not have to modify.
******************************************************************/

struct event {
    float evtime;  /* event time */
    int evtype;  /* event type code */
    int eventity;  /* entity where event occurs */
    struct frm *frmptr;  /* ptr to frame (if any) associated with this event */
    struct event *prev;
    struct event *next;
};
struct event *evlist = NULL;  /* the event list */

/* possible events */
#define TIMER_INTERRUPT 0
#define FROM_LAYER3 1
#define FROM_LAYER1 2

#define OFF 0
#define ON 1
#define A 0
#define B 1

int TRACE = 1;  /* for my debugging */
int nsim = 0;  /* number of packets from 3 to 2 so far */
int nsimmax = 0;  /* number of pkts to generate, then stop */
float time = 0.000;
float lossprob;  /* probability that a frame is dropped  */
float corruptprob;  /* probability that one bit in frame is flipped */
float lambda;  /* arrival rate of packets from layer 3 */
int ntolayer1;  /* number sent into layer 1 */
int nlost;  /* number lost in media */
int ncorrupt;  /* number corrupted by media*/

void init();
void generate_next_arrival(void);
void insertevent(struct event *p);

int main(void) {
    struct event *eventptr;
    struct pkt pkt2give;
    struct frm frm2give;

    int i, j;

    init();
    printf("\n");
    A_init();
    B_init();

    while(1) {
        eventptr = evlist;  /* get next event to simulate */
        if(eventptr == NULL) goto terminate;
        evlist = evlist->next;  /* remove this event from event list */
        if(evlist != NULL) evlist->prev = NULL;

        if(TRACE >= 2) {
            printf("\n>> EVENT time: %f,", eventptr->evtime);
            printf("  type: %d", eventptr->evtype);
            if(eventptr->evtype == TIMER_INTERRUPT) printf(", timerinterrupt,");
            else if(eventptr->evtype == FROM_LAYER3) printf(", fromlayer3,");
            else printf(", fromlayer1,");
            printf(" entity: %d\n", eventptr->eventity);
        }
        time = eventptr->evtime;  /* update time to next event time */

        if(eventptr->evtype == FROM_LAYER3) {
            if(nsim < nsimmax) {
                if(nsim+1 < nsimmax) generate_next_arrival();  /* set up future arrival */
                /* fill in payload to give with string of same letter */
                j = nsim%26;
                for(i=0; i<4; i++) pkt2give.payload[i] = 97+j;
                pkt2give.payload[3] = 0;

                if(TRACE > 2) {
                    printf(">> MAINLOOP: packet given to student: ");
                    for(i=0; i<4; i++) printf("%c", pkt2give.payload[i]);
                    printf("\n");
                }
                nsim++;

                if(eventptr->eventity == A) A_output(pkt2give);
                else B_output(pkt2give);
            }
        } else if(eventptr->evtype == FROM_LAYER1) {
            frm2give.type = eventptr->frmptr->type;
            frm2give.seqnum = eventptr->frmptr->seqnum;
            frm2give.acknum = eventptr->frmptr->acknum;
            frm2give.checksum = eventptr->frmptr->checksum;
            for(i=0; i<4; i++) frm2give.payload[i] = eventptr->frmptr->payload[i];

            if(eventptr->eventity == A)  /* deliver frame by calling */ A_input(frm2give);  /* appropriate entity */
            else B_input(frm2give);
            free(eventptr->frmptr);  /* free the memory for frame */
        } else if(eventptr->evtype == TIMER_INTERRUPT) {
            if(eventptr->eventity == A) A_timerinterrupt();
            else B_timerinterrupt();
        } else {
            printf(">> INTERNAL PANIC: unknown event type\n");
        }
        free(eventptr);
    }

terminate:
    printf("\nSimulator terminated at time %f\n after sending %d packets from layer3\n\n", time, nsim);
}

void init()  /* initialize the simulator */ {
    int i;
    float sum, avg;
    float jimsrand();

    printf("----- Stop and Wait Network Simulator Version 1.1 --------\n\n");
    printf("Enter the number of packets to simulate: ");
    scanf("%d", &nsimmax);
    printf("Enter frame loss probability [enter 0.0 for no loss]: ");
    scanf("%f", &lossprob);
    printf("Enter frame corruption probability [enter 0.0 for no corruption]: ");
    scanf("%f", &corruptprob);
    printf("Enter average time between packets from sender's layer3 [ > 0.0]: ");
    scanf("%f", &lambda);
    printf("Enter TRACE: ");
    scanf("%d", &TRACE);
    printf("Enter generator polynomial [in binary format such as 11001]: ");
    scanf("%d", &generator_polynomial);
    printf("Show CRC steps? [0 -> N | 1 -> Y]: ");
    scanf("%d", &crc_steps);
    printf("Enable Piggybacking? [0 -> N | 1 -> Y]: ");
    scanf("%d", &piggybacking);

    srand(9999);  /* init random number generator */
    sum = 0.0;  /* test random number generator for students */
    for(i=0; i<1000; i++) sum = sum+jimsrand();  /* jimsrand() should be uniform in [0, 1] */
    avg = sum/1000.0;
    if(avg<0.25 || avg>0.75) {
        printf(">> It is likely that random number generation on your machine\n");
        printf(">> is different from what this emulator expects. Please take\n");
        printf(">> a look at the routine jimsrand() in the emulator code. Sorry.\n");
        exit(1);
    }

    ntolayer1 = 0;
    nlost = 0;
    ncorrupt = 0;

    time = 0.0;  /* initialize time to 0.0 */
    generate_next_arrival();  /* initialize event list */
}

/****************************************************************************/
/* jimsrand(): return a float in range [0, 1]. The routine below is used to */
/* isolate all random number generation in one location. We assume that the */
/* system-supplied rand() function return an int in the range [0, mmm] */
/****************************************************************************/
float jimsrand(void) {
    double mmm = RAND_MAX;
    float x;  /* individual students may need to change mmm */
    x = rand()/mmm;  /* x should be uniform in [0, 1] */
    return x;
}

/********************* EVENT HANDLER ROUTINES *******/
/* The next set of routines handles the event list */
/*****************************************************/
void generate_next_arrival(void) {
    double x;
    struct event *evptr;

    if(TRACE > 2) printf(">> GENERATE NEXT ARRIVAL: creating new arrival\n");

    x = lambda*jimsrand()*2;  /* x is uniform in [0, 2*lambda] */
    /* having mean of lambda */
    evptr = (struct event*) malloc(sizeof(struct event));
    evptr->evtime = time+x;
    evptr->evtype = FROM_LAYER3;
    if(BIDIRECTIONAL && (jimsrand() > 0.5)) evptr->eventity = B;
    else evptr->eventity = A;
    insertevent(evptr);
}

void insertevent(struct event *p) {
    struct event *q, *qold;

    if(TRACE > 2) {
        printf(">> INSERTEVENT: time is %lf\n", time);
        printf(">> INSERTEVENT: future time will be %lf\n", p->evtime);
    }
    q = evlist;  /* q points to header of list in which p struct inserted */
    if(q == NULL)  /* list is empty */ {
        evlist = p;
        p->next = NULL;
        p->prev = NULL;
    } else {
        for(qold=q; q!=NULL && p->evtime>q->evtime; q=q->next) qold = q;
        if(q == NULL)  /* end of list */ {
            qold->next = p;
            p->prev = qold;
            p->next = NULL;
        } else if(q == evlist)  /* front of list */ {
            p->next = evlist;
            p->prev = NULL;
            p->next->prev = p;
            evlist = p;
        } else  /* middle of list */ {
            p->next = q;
            p->prev = q->prev;
            q->prev->next = p;
            q->prev = p;
        }
    }
}

void printevlist(void) {
    struct event *q;
    int i;
    printf("--------------\nEvent List Follows:\n");
    for(q=evlist; q!=NULL; q=q->next) {
        printf("Event time: %f, type: %d entity: %d\n", q->evtime, q->evtype, q->eventity);
    }
    printf("--------------\n");
}

/********************** Student-callable ROUTINES ***********************/

/* called by student's routine to cancel a previously-started timer */
void stoptimer(int AorB  /* A or B is trying to stop timer */) {
    struct event *q;

    if(TRACE > 2) printf(">> STOP TIMER: stopping timer at %f\n", time);
    /* for(q=evlist; q!=NULL && q->next!=NULL; q=q->next) */
    for(q=evlist; q!=NULL; q=q->next) {
        if(q->evtype==TIMER_INTERRUPT && q->eventity==AorB) {
            /* remove this event */
            if(q->next==NULL && q->prev==NULL) evlist = NULL;  /* remove first and only event on list */
            else if(q->next == NULL)  /* end of list - there is one in front */ q->prev->next = NULL;
            else if(q == evlist)  /* front of list - there must be event after */ {
                q->next->prev = NULL;
                evlist = q->next;
            } else  /* middle of list */ {
                q->next->prev = q->prev;
                q->prev->next = q->next;
            }
            free(q);
            return ;
        }
    }
    printf(">> Warning: unable to cancel your timer. It wasn't running.\n");
}

void starttimer(int AorB  /* A or B is trying to start timer */, float increment) {
    struct event *q, *evptr;

    if(TRACE > 2) printf(">> START TIMER: starting timer at %f\n", time);
    /* be nice: check to see if timer is already started, if so, then warn */
    /* for(q=evlist; q!=NULL && q->next!=NULL; q=q->next) */
    for(q=evlist; q!=NULL; q=q->next) {
        if(q->evtype==TIMER_INTERRUPT && q->eventity==AorB) {
            printf(">> Warning: attempt to start a timer that is already started\n");
            return ;
        }
    }

    /* create future event for when timer goes off */
    evptr = (struct event*) malloc(sizeof(struct event));
    evptr->evtime = time+increment;
    evptr->evtype = TIMER_INTERRUPT;
    evptr->eventity = AorB;
    insertevent(evptr);
}

/************************** TOLAYER1 ***************/
void tolayer1(int AorB, struct frm frame) {
    struct frm *myfrmptr;
    struct event *evptr, *q;
    float lastime, x;
    int i;

    ntolayer1++;

    /* simulate losses */
    if(jimsrand() < lossprob) {
        nlost++;
        if(TRACE > 0) printf(">> TOLAYER1: frame being lost\n");
        return ;
    }

    /* make a copy of the frame student just gave me since he/she may decide */
    /* to do something with the frame after we return back to him/her */
    myfrmptr = (struct frm*) malloc(sizeof(struct frm));
    myfrmptr->type = frame.type;
    myfrmptr->seqnum = frame.seqnum;
    myfrmptr->acknum = frame.acknum;
    myfrmptr->checksum = frame.checksum;
    for(i=0; i<4; i++) myfrmptr->payload[i] = frame.payload[i];
    if(TRACE > 2) {
        printf(">> TOLAYER1: type: %d, seq: %d, ack %d, check: %d, ", myfrmptr->type, myfrmptr->seqnum, myfrmptr->acknum, myfrmptr->checksum);
        for(i=0; i<4; i++) printf("%c", myfrmptr->payload[i]);
        printf("\n");
    }

    /* create future event for arrival of frame at the other side */
    evptr = (struct event*) malloc(sizeof(struct event));
    evptr->evtype = FROM_LAYER1;  /* frame will pop out from layer1 */
    evptr->eventity = (AorB+1)%2;  /* event occurs at other entity */
    evptr->frmptr = myfrmptr;  /* save ptr to my copy of frame */

    /* finally, compute the arrival time of frame at the other end.
       medium can not reorder, so make sure frame arrives between 1 and 10
       time units after the latest arrival time of frames
       currently in the medium on their way to the destination */
    lastime = time;
    /* for(q=evlist; q!=NULL && q->next!=NULL; q=q->next) */
    for(q=evlist; q!=NULL; q=q->next) {
        if(q->evtype==FROM_LAYER1 && q->eventity==evptr->eventity) lastime = q->evtime;
    }
    evptr->evtime = lastime+1+9*jimsrand();

    /* simulate corruption */
    if(jimsrand() < corruptprob) {
        ncorrupt++;
        if((x = jimsrand()) < .75) myfrmptr->payload[0] = 'Z';  /* corrupt payload */
        else if(x < .875) myfrmptr->seqnum = 999999;
        else myfrmptr->acknum = 999999;
        if(TRACE > 0) printf(">> TOLAYER1: frame being corrupted\n");
    }

    if(TRACE > 2) printf(">> TOLAYER1: scheduling arrival on other side\n");
    insertevent(evptr);
}

void tolayer3(int AorB, struct pkt packet) {
    int i;
    if(TRACE > 2) {
        printf(">> TOLAYER3: packet received.\n");
        printf(">> TOLAYER3: seq: %d, ack %d, check: %d, ", packet.seqnum, packet.acknum, packet.checksum);
        for(i=0; i<4; i++) printf("%c", packet.payload[i]);
        printf("\n");
    }
}
