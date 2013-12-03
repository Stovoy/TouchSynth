#include "LiquidCrystal.h"

LiquidCrystal lcd(39, 38, 41, 40, 51, 50);
  
void LCDInit()
{
  lcd.begin(16,2);
  byte LArrow[8] = 
  {
    B00000,
    B00000,
    B00100,
    B01100,
    B11111,
    B01100,
    B00100,
    B00000
  };
  
  byte RArrow[8] = 
  {
    B00000,
    B00000,
    B00100,
    B00110,
    B11111,
    B00110,
    B00100,
    B00000
  };
  
  byte CBubble[8] = 
  {
    B00000,
    B01110,
    B10001,
    B10101,
    B10101,
    B10001,
    B01110,
    B00000
  };
  
  byte OBubble[8] = 
  {
    B00000,
    B01110,
    B10001,
    B10001,
    B10001,
    B10001,
    B01110,
    B00000
  };
  
  byte Box[8] = 
  {
    B11111,
    B11111,
    B11111,
    B11111,
    B11111
  };
  lcd.createChar(0, LArrow);
  lcd.createChar(1, RArrow);
  lcd.createChar(2, CBubble);
  lcd.createChar(3, OBubble);
  lcd.createChar(4, Box);
}

void LCDDisplayTitle()
{
  LCDInit();
  startTimer(&clearLCD, 8000);
  lcd.clear();
  lcd.setCursor(3, 0);
  lcd.print("Welcome To");
  lcd.setCursor(3, 1);
  lcd.print("TouchSynth");
}

void LCDDisplayNoteInfo(int row, int column)
{
  row = 16-row;
  
  startTimer(&clearLCD, 5000);
  char noteNames[24][4] = {"A3 ", "A#3", "B3 ", "C4 ", "C#4", "D4 ", "D#4", "E4 ", "F4 ", "F#4", "G4 ", "G#4", "A4 ", "A#4", "B4 ", "C5 ", "C#5", "D5 ", "D#5", "E5 ", "F5 ", "F#5", "G5 ", "G#5"};
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Note: ");
  for (int i = 0; i < 3; ++i) lcd.print(noteNames[row-1][i]);
  lcd.print(" ");
  lcd.print(int(notes[row-1]));
  lcd.setCursor(14, 0);
  lcd.print("hz");
  lcd.setCursor(0, 1);
  lcd.print("Row: ");
  lcd.print(row);
  if (column > 99) lcd.setCursor(8, 1);
  else lcd.setCursor(9, 1);      
  lcd.print("Col: ");
  lcd.print(column);
}

void clearLCD()
{
  if (!menuActivated)
  {
    LCDInit();
    lcd.clear();
  }
  stopTimer();
}
