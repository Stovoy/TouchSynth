void menuInit()
{
  menuActivated = false;
  for (int i = 0; i < 3; ++i) menuPos[i] = 0;
  menuDepth = 0;
}

void menuUp()
{
  if (menuPos[menuDepth] == 0) return;
  --menuPos[menuDepth];
  menuDisplay();  
}

void menuDown()
{
  if (menuDepth == 0 && menuPos[0] == size1) return;
  else if ((menuDepth == 1 && ((menuPos[1] == size2 || (menuPos[1] == 1 && menuPos[0] == 1)))) || (menuDepth == 2 && ((menuPos[2] == 1 && menuPos[1] == 1) || (menuPos[2] == 2 && menuPos[1] == 0)))) return;
  ++menuPos[menuDepth];
  menuDisplay();
}

void menuIn()
{
  int state;
  if (menuDepth == 0) state = state1[menuPos[0]];
  else if (menuDepth == 1) state = state2[menuPos[1]];
  if (state == 2 || menuDepth == 1 && menuPos[0] == 1) 
  {
    menuPos[++menuDepth] = 0;
    LCDInit();
    menuDisplay();
    return;
  }
  else if (menuPos[0] == 5)
  {
      if (++numColumnSets > 48) numColumnSets = 48;
  }
  if (menuDepth == 2)
  {
    byte* settings;
    if (menuPos[1] == 0) settings = &localSettings[xGrid/16];
    else settings = &globalSettings;
      
    if (menuPos[2] == 0)
    {
      byte tempo = (*settings & B00011110) >> 1;
      if (tempo < 15) ++tempo;
      *settings = ((*settings & B11100000) + (tempo << 1)) + (*settings & B00000001);
    }
    else if (menuPos[1] == 0)
    {
      *settings ^= 1;
    }
  }
  menuDisplay();
}

void menuOut()
{
  int state = 0;
  if (menuDepth == 0) state = state1[menuPos[0]];
  if (menuDepth == 0 && state != 3)
  {
    menuActivated = false;
    LCDInit();
    menuPos[0] = 0;
    return;
  }
  if (menuPos[0] == 5)
  {
    if (--numColumnSets < 1) numColumnSets = 1;
    menuDisplay();
    return;    
  }
  if (menuDepth == 2 && ((menuPos[1] == 0 && menuPos[2] != 2) || (menuPos[1] == 1 && menuPos[2] != 1)))
  {
    byte* settings;
    if (menuPos[1] == 0) settings = &localSettings[xGrid/16];
    else settings = &globalSettings;
    if (menuPos[2] == 0)
    {
      byte tempo = (*settings & B00011110) >> 1;
      if (tempo > 1) --tempo;
      *settings = ((*settings & B11100000) + (tempo << 1)) + (*settings & B00000001); 
    }
    else if (menuPos[1] == 0)
    {
      *settings ^= 1;
    }
    menuDisplay();
    return;
  }
  LCDInit();
  --menuDepth;
  menuDisplay();
}

void menuDisplay()
{
  int state = 4;
  if (menuDepth == 0) state = state1[menuPos[0]];
  lcd.clear();
  lcd.setCursor(0, 0);
  if (menuDepth == 0) lcd.print(menu1[menuPos[0]]);
  else if (menuDepth == 1)
  {
    if (menuPos[0] != 1)
      lcd.print(menu2[menuPos[1]]);
    else
      lcd.print(menu3[menuPos[1]]);
  }
  else if (menuDepth == 2)
  {
    if (menuPos[1] == 1) lcd.print(menu4[menuPos[2]]);
    else lcd.print(menu5[menuPos[2]]);
    lcd.setCursor(5, 1);
    if (menuPos[1] == 0) 
    {
      if (menuPos[2] == 0) lcd.print((localSettings[xGrid/16] & B00011110) >> 1);
    }
    else
    {
      if (menuPos[2] == 0) lcd.print((globalSettings & B00011110) >> 1);
    }
  }
  lcd.setCursor(0, 1);
  lcd.write(0);
  if (menuPos[0] == 5)
  {
    lcd.setCursor(5, 1);
    lcd.print(numColumnSets*16);
  }
  lcd.setCursor(15, 1);
  if (menuDepth == 1 && menuPos[0] == 1) state = 2;
  else if (menuDepth == 2 && menuPos[2] < 1) state = 3;
  else if (menuDepth == 2 && menuPos[1] == 0 && menuPos[2] == 1) state = localSettings[xGrid/16] & B00000001;
  if (menuPos[0] < 7 || state == 3)
  {
    switch (state)
    {
      case 0:
        lcd.write(3);
        break;
      case 1:
        lcd.write(2);
        break;
      case 2:
      case 3:
        lcd.write(1);
        break;
    }
  }
}
