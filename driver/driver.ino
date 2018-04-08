#define LOG_OUT 1
#define FFT_N 256
#include <math.h>
#include <avr/pgmspace.h>
/**
 * USES Pin 0-11
 * 
 * PIN0 SAMMING
 */
char buff[2];
int cnt = 0;

//MIDI69: ラ, FING_STEP[9]
const PROGMEM unsigned short FING_STEP[27][11] = {
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH},
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,HIGH,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,HIGH,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,LOW ,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,HIGH,LOW ,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,LOW ,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,LOW ,LOW ,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {LOW ,LOW ,LOW ,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,HIGH,LOW ,LOW ,HIGH,HIGH},
  {HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,HIGH,HIGH,HIGH},
  {HIGH,HIGH,HIGH,HIGH,LOW ,LOW ,LOW ,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,HIGH,HIGH,LOW ,LOW },
  {HIGH,HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,LOW ,LOW ,HIGH,HIGH,LOW ,LOW ,LOW ,LOW },
  {HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,HIGH,LOW ,LOW ,HIGH,HIGH},
  {HIGH,HIGH,HIGH,LOW ,HIGH,HIGH,LOW ,HIGH,HIGH,LOW ,LOW }
};

void setup()
{
  Serial.begin(9600);
  for(int i = 1; i < 10; i++)
  {
    pinMode(i , OUTPUT);
  }
  TIMSK0 = 0;
  ADCSRA = 0xe5;
  ADMUX = 0x40;
  DIDR0 = 0x01;
}

void loop()
{
  cnt = 0;
  int val = 0;
  while(Serial.available())
  {
    buff[cnt] = Serial.read();
    if(cnt == 1)
      break;
    else
      cnt++;
  }
  buff[cnt] = "\0";
  if(cnt != 0)
  {
    val = atoi(buff);
    Serial.print("Current MIDI Note is: ");
    Serial.println(val, DEC);
    if(!(val < 0 || val > 26))
    {
      Play(FING_STEP[val]);
    }
    else if(val == 28)
    {
      for(short i = 0; i < 11; i++)
      {
        digitalWrite(i,LOW);
      }
    }
  }
}

void Play(short fstep[11])
{
  for(short i = 0; i < 11; i++)
  {
    if(fstep[i] == 2)
    {
      continue;
    }
    digitalWrite(i,fstep[i]);
  }
}
