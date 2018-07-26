
bool nokill = true;
int wheelSpeed, steeringDeg;
int wheelDelay = 1500;
int steerDelay = 1500;
int normalDelay = 1000;
byte[3] out = new byte[3];
//where 1ms is considered full left or full reverse, and 2ms is considered full forward or full right.

void setup() {
  // put your setup code here, to run once:
  //set pins to input/output
  pinMode(9, OUTPUT); //steering
  pinMode(10, OUTPUT); //speed
  pinMode(11, INPUT); //speed getter
  pinMode(2, INPUT); //Dead man's switch
  
  pinMode(13, OUTPUT); //testing light
  digitalWrite(13, HIGH);

  Serial.begin(57600);
  
  
}

void loop() {
  // put your main code here, to run repeatedly:
  //grab input from kill switch
  
  
  if (nokill){
    //read input from computer
    while (Serial.available() > 0){
      digitalWrite(13, LOW);
      byte type = Serial.read();
      byte pin = Serial.read();
      byte value = Serial.read();

      if (pin == 9){
        if (value != steeringDeg){
          //steerDelay = 1.0+((double) value)/180;
          steerDelay = map(value, 0, 180, 1000, 2000);
        }
        steeringDeg = value;
      }
      if (pin == 10){
        if (value != wheelSpeed){
          //wheelDelay = 1.0+((double) value)/180;
          wheelDelay = map(value, 0, 180, 1000, 2000);
        }
        wheelSpeed = value;
      }
      //create normalizer delay to keep hertz more consistant
      normalDelay = 4000 - wheelDelay - steerDelay;

      //sensor in: 5V to 0
    }
    digitalWrite(13, HIGH);
    
    //Steering
    digitalWrite(9, HIGH); 
    delayMicroseconds(steerDelay); //create pulse timing
    digitalWrite(9, LOW);

    //Speed control
    digitalWrite(10, HIGH);
    delayMicroseconds(wheelDelay);
    digitalWrite(10, LOW);
    
    delayMicroseconds(normalDelay); //normalize to ~2ms total
    out[0] = 1;
    out[1] = 11;
    out[2] = (char) 'L';
    
    if (sizeof(out) >= 3){
      Serial.write(out, 3);
    }
  } else {
    //send message to main program
    
    //discard info from computer while dead
    while (Serial.available() > 0){
      Serial.read();
    }
  }
  delay(16); //add up total delay to .02 seconds (20ms), leading to 50hz.
}

//jssc
//
