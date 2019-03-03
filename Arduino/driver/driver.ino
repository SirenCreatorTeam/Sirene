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

//MIDI69: ラ, FING_STEP[9]
const unsigned int FING_STEP[27][13] = {
  {0,1,1,1,1,1,1,1,1,1,1,166}, //ド
  {0,1,1,1,1,1,1,1,1,1,0,166}, //ド#
  {0,1,1,1,1,1,1,1,1,0,0,164}, //レ
  {0,1,1,1,1,1,1,1,0,0,0,164}, //レ#
  {0,1,1,1,1,1,1,0,0,0,0,163}, //ミ
  {0,1,1,1,1,1,0,0,0,0,0,163}, //ファ
  {0,1,1,1,1,0,1,1,1,0,0,165}, //ファ#
  {0,1,1,1,1,0,0,0,0,0,0,161}, //ソ
  {0,1,1,1,0,1,1,1,0,0,0,164}, //ソ#
  {0,1,1,1,0,0,0,0,0,0,0,161}, //ラ
  {0,1,1,0,1,1,0,0,0,0,0,159}, //ラ#
  {0,1,1,0,0,0,0,0,0,0,0,159}, //シ
  {0,1,0,1,0,0,0,0,0,0,0,159}, //ド
  {0,0,1,1,0,0,0,0,0,0,0,157}, //ド#
  {0,0,0,1,0,0,0,0,0,0,0,157}, //レ
  {0,0,0,1,1,1,1,1,1,0,0,157}, //レ#
  {1,1,1,1,1,1,1,0,0,0,0,157}, //ミ
  {1,1,1,1,1,1,0,0,0,0,0,157}, //ファ
  {1,1,1,1,1,0,1,1,0,0,0,156}, //ファ#
  {1,1,1,1,1,0,0,0,0,0,0,156}, //ソ
  {1,1,1,1,1,0,1,1,1,1,1,155}, //ソ#
  {1,1,1,1,0,0,0,0,0,0,0,151}, //ラ Fails from
  {1,1,1,1,0,1,1,1,1,0,0,151}, //ラ#
  {1,1,1,1,0,1,1,0,0,0,0,150}, //シ
  {1,1,1,0,0,1,1,0,0,0,0,150}, //ド
  {1,1,1,0,1,1,1,0,0,1,1,148}, //ド#
  {1,1,1,0,1,1,0,1,1,0,0,148}  //レ
};

void setup()
{
  Serial.begin(9600);
  Serial.setTimeout(600000UL);
  pinMode(2, OUTPUT);
  for(int i = 3; i < 14; i++)
  {
    pinMode(i , OUTPUT);
  }
  analogWrite(3, 255);
}

void loop()
{
  int val = 0;
  String readed = Serial.readStringUntil('.');
  if(!isNumber(readed))
  {
#ifdef DEBUG
    Serial.print("'");
    Serial.print(readed.c_str());
    Serial.println("' is not a valid number.");
#endif
    return;
  }
  val = atoi(readed.c_str());
  if(0 <= val && val <= 26)
  {
    Play(val);
  }
  else if(val == 27)
  {
    analogWrite(3, 255);
    for(short i = 0; i < 14; i++)
    {
      if(i == 0)
        digitalWrite(2, 0);
      else
        digitalWrite(i+3, 0);
    }
  }
  else if(val == 28)
  {
    analogWrite(3, 255);
  }
}

void Play(short num)
{
  analogWrite(3, FING_STEP[num][11]);
  //Serial.println(FING_STEP[num][11]);
  for(short i = 0; i < 11; i++)
  {
    if(i == 0)
    {
      digitalWrite(2, FING_STEP[num][i]);
#ifdef DEBUG
      Serial.print("Thumbing: ");
      Serial.println(FING_STEP[num][i]);
#endif
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

bool isNumber(String str)
{
  if(0 >= str.length())
    return false;
  int len = str.length();
  for(int i = 0; i < str.length(); i++)
  {
    if(isSpace(str.charAt(i)))
    {
      len--;
      continue;
    }
    if(!isDigit(str.charAt(i)))
      return false;
  }
  return 0 < len;
}

