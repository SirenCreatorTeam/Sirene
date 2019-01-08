#include <math.h>
/*
#include <avr/pgmspace.h>
*/
#define DEBUG 0
/**
 * ピン3-13を使用中
 * 
 * ピン3は空気量の調節用ピン(PWM)です。
 * ピン4-13は指制御です。
 * ピン2はサミング制御です。
 */
 /**
  * Pin 3-13 in use.
  * Pin3 is used for air current control (PWM)
  * Pin4-13 is used for finger operation control
  * Pin2 is used for thumbing control.
  */

//MIDI69: ラ, FING_STEP[9]
const unsigned short FING_STEP[27][12] = {
  {1,0,0,0,0,0,0,0,0,0,0},
  {1,0,0,0,0,0,0,0,0,0,1},
  {1,0,0,0,0,0,0,0,0,1,1},
  {1,0,0,0,0,0,0,0,1,1,1},
  {1,0,0,0,0,0,0,1,1,1,1},
  {1,0,0,0,0,0,1,1,1,1,1},
  {1,0,0,0,0,1,0,0,0,1,1},
  {1,0,0,0,0,1,1,1,1,1,1},
  {1,0,0,0,1,0,0,0,1,1,1},
  {1,0,0,0,1,1,1,1,1,1,1},
  {1,0,0,1,0,0,1,1,1,1,1},
  {1,0,0,1,1,1,1,1,1,1,1},
  {1,0,1,0,1,1,1,1,1,1,1},
  {1,1,0,0,1,1,1,1,1,1,1},
  {1,1,1,0,1,1,1,1,1,1,1},
  {1,1,1,0,0,0,0,0,0,1,1},
  {0,0,0,0,0,0,0,1,1,1,1},
  {0,0,0,0,0,0,1,1,1,1,1},
  {0,0,0,0,0,1,0,1,1,0,0},
  {0,0,0,0,0,1,1,1,1,1,1},
  {0,0,0,0,0,1,0,0,0,0,0},
  {0,0,0,0,1,1,1,1,1,1,1},
  {0,0,0,0,1,0,0,0,0,1,1},
  {0,0,0,0,1,0,0,1,1,1,1},
  {0,0,0,1,1,0,0,1,1,1,1},
  {0,0,0,1,0,0,0,1,1,0,0},
  {0,0,0,1,0,0,1,0,0,1,1}
};

void setup()
{
  Serial.begin(9600);
  pinMode(2, OUTPUT);
  for(int i = 3; i < 14; i++)
  {
    pinMode(i, OUTPUT);
  }
}

void loop()
{
  int val = 0;
  String readed = Serial.readStringUntil('.');
  val = atoi(readed.c_str());
#ifdef DEBUG
  if(val != 28)
  {
    Serial.print("Current MIDI Note is: ");
    Serial.println(val, DEC);
  }
  else
  {
    Serial.println("Output all off.");
    Serial.println("00000000000");
  }
#endif
  if(0 <= val && val <= 26)
  {
    Play(val);
  }
  else if(val == 28)
  {
    for(short i = 0; i < 14; i++)
    {
      if(i == 0)
        digitalWrite(2, 1);
      else
        digitalWrite(i+3, 1);
    }
  }
}

void Play(short num)
{
  for(short i = 0; i < 11; i++)
  {
    if(i == 0)
    {
      digitalWrite(2, FING_STEP[num][i]);
      Serial.print("Thumbing: ");
      Serial.println(FING_STEP[num][i]);
    }
    else
      digitalWrite(i+3, FING_STEP[num][i]);
#if DEBUG
    Serial.print(FING_STEP[num][i]);
  }
  Serial.print("\n");
#else
  }
#endif
}

