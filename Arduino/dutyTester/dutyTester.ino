#include <math.h>

int note = 0;

const unsigned int FING_STEP[27][13] = {
  {0,1,1,1,1,1,1,1,1,1,1,169}, //ド
  {0,1,1,1,1,1,1,1,1,1,0,169}, //ド#
  {0,1,1,1,1,1,1,1,1,0,0,168}, //レ
  {0,1,1,1,1,1,1,1,0,0,0,166}, //レ#
  {0,1,1,1,1,1,1,0,0,0,0,165}, //ミ
  {0,1,1,1,1,1,0,0,0,0,0,165}, //ファ
  {0,1,1,1,1,0,1,1,1,0,0,165}, //ファ# Fails
  {0,1,1,1,1,0,0,0,0,0,0,165}, //ソ
  {0,1,1,1,0,1,1,1,0,0,0,163}, //ソ#
  {0,1,1,1,0,0,0,0,0,0,0,162}, //ラ
  {0,1,1,0,1,1,0,0,0,0,0,164}, //ラ#
  {0,1,1,0,0,0,0,0,0,0,0,162}, //シ
  {0,1,0,1,0,0,0,0,0,0,0,162}, //ド
  {0,0,1,1,0,0,0,0,0,0,0,161}, //ド#
  {0,0,0,1,0,0,0,0,0,0,0,161}, //レ
  {0,0,0,1,1,1,1,1,1,0,0,160}, //レ#
  {1,1,1,1,1,1,1,0,0,0,0,162}, //ミ
  {1,1,1,1,1,1,0,0,0,0,0,162}, //ファ
  {1,1,1,1,1,0,1,0,0,1,1,160}, //ファ#
  {1,1,1,1,1,0,0,0,0,0,0,155}, //ソ
  {1,1,1,1,1,0,1,1,1,1,1,154}, //ソ#
  {1,1,1,1,0,0,0,0,0,0,0,154}, //ラ
  {1,1,1,1,0,1,1,1,1,0,0,154}, //ラ#
  {1,1,1,1,0,1,1,0,0,0,0,153}, //シ
  {1,1,1,0,0,1,1,0,0,0,0,152}, //ド
  {1,1,1,0,1,1,1,0,0,1,1,152}, //ド#
  {1,1,1,0,1,1,0,1,1,0,0,151}  //レ
};

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(2000u);
  for(int i  = 2; i < 14; i++)
  {
    if(i == 3)
      continue;
    pinMode(i, OUTPUT);
  }
  analogWrite(3, 255);
}

void loop() {
  // put your main code here, to run repeatedly:
  int val = 0;
  String str = Serial.readStringUntil('.');
  //Serial.read();
  //Serial.println(str.length());
  if(str.length() == 0)
    return;
  if(!isNumber(str))
    note++;
  if(note > 26)
    note = 0;
  Play(note);
  val = 255 - atoi(str.c_str());
  Serial.print("Air current: ");
  Serial.println(val);
  Serial.print("Note id: ");
  Serial.println(note);
  if(0 > val and 255 < val)
    return;
  analogWrite(3, val);
  delay(600);
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
