#include <Arduino.h>

int c = 0;

void setup()
{
  Serial.begin(9600);
  for(int i = 0; i < 12; i++)
    pinMode(i+2, OUTPUT);
  analogWrite(3, 255);
}

void loop()
{
  for(int i = 0; i < 12; i++)
  {
    if(i == 1)
      continue;
    while(1)
    {
      digitalWrite(i+2, HIGH);
      delay(500);
      digitalWrite(i+2, LOW);
      delay(500);
      if(Serial.available() > 0)
      {
        while(Serial.available() > 0)
          Serial.read();
        break;
      }
    }
  }
}

