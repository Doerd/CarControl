
#include <Firmata.h>
int steeringDeg;
bool nokill = true;
double wheelspeed;
double steering;

void setup() {
  // put your setup code here, to run once:
  //set pins to input/output
  pinMode(9, OUTPUT); //steering
  pinMode(10, OUTPUT); //speed
  pinMode(11, INPUT); //speed getter
  pinMode(2, INPUT); //Dead man's switch
  
  pinMode(13, OUTPUT); //testing light
  digitalWrite(13, HIGH);

}

void processCompInput(byte pin, int value){
  //sendAnalog(ANALOG_MESSAGE
}

void loop() {
  // put your main code here, to run repeatedly:
  //grab input from kill switch
  
  if (nokill){
    //read input from computer
    while (Firmata.available()){
      Firmata.processInput();  
    }

    //Steering
    digitalWrite(9, HIGH);
    delay(steering); //create pulse timing
    digitalWrite(9, LOW);

    //Speed control
    digitalWrite(10, HIGH);
    delay(wheelspeed);
    digitalWrite(10, LOW);

    delay(4-steering-wheelspeed); //normalize to ~2ms

    
  } else {
    //send message to main program
    //discard info from computer?
  }
  delay(16); //add up total delay to .02 seconds (20ms), leading to 50hz.
}

//jssc
//
