int getRawTouchscreenX()
{
  int x;

  pinMode(DB, OUTPUT); 
  digitalWrite(DB, LOW);
  pinMode(DT, OUTPUT); 
  digitalWrite(DT, HIGH); 
  pinMode(DR, INPUT); 
  pinMode(DL, INPUT); 
  delay(10);
  x = analogRead(AR);
  return x;
}

int getRawTouchscreenY()
{
  int y;

  pinMode(DL, OUTPUT); 
  digitalWrite(DL, LOW); 
  pinMode(DR, OUTPUT); 
  digitalWrite(DR, HIGH); 
  pinMode(DT, INPUT);
  pinMode(DB, INPUT);
  delay(10);
  y = analogRead(AT);
  return y;
}

int getTouchscreenX()
{
  int x = getRawTouchscreenX();
  
  if (x > TSThreshold) return -3;
  if (x < TSLeft)
  {
    if (x < TSLeft - 10) return -1;
    return -4;
  }
  else if (x > TSRight)
  {
    if (x > TSRight + 10) return -2;
    return -4;
  }
  x = ((x-TSLeft) * float(1000))/float(TSRight-TSLeft);

  return x;
}

int getTouchscreenY()
{
  int y = getRawTouchscreenY();
  
  if (y > TSThreshold) return -3;
  if (y < TSBottom)
  {
    if (y < TSBottom - 10) return -1;
    return -4;
  }
  else if (y > TSTop)
  {
    if (y > TSTop + 10) return -2;
    return -4;
  }
  y = ((y-TSBottom) * float(1000))/float(TSTop-TSBottom);
  return y;
}

void pollTouchscreen()
{
  if (millis() - TSLastChanged >= TSTime)
  {
    int x = getTouchscreenX();
    int y = getTouchscreenY();
    ++TSSameCount;

    TSStored[TSSameCount][0] = x;
    TSStored[TSSameCount][1] = y;

    int range = 15;
    for (int i = TSSameCount; i > 0; --i)
    {
      if ((TSStored[i][0] < TSStored[i-1][0] - range || TSStored[i][0] > TSStored[i-1][0] + range) || (TSStored[i][1] < TSStored[i-1][1] - range || TSStored[i][1] > TSStored[i-1][1] + range)) TSSameCount = -1;
    }
    if (TSSameCount > TSStoredSize-1) TSSameCount = TSStoredSize-1;
    if (TSSameCount < TSStoredSize-1) return;
    if ((x < 0 || y < 0))
    {
      TSScroll = true;
      for (int i = 0; i < 4; ++i) buttonState[i] = true;
      if (x == -1) d();
      else if (x == -2) u();
      if (y == -1) l();
      else if (y == -2) r();

      pressed = false;
      return;
    }
    TSLastChanged = millis();
    int row = round(x / 122.5);
    int column = round(y / 122.5)-1;
    if (column < 0) column = 0;
    row = 8 - row;

    if (row < 0 || row > 7 || column < 0 || column > 7 || 23-yGrid-row < 0) return;
    if (!pressed) set = !checkLED_Selected(row, column);
    pressed = true;

    if (TSLastPressed[0] != row || TSLastPressed[1] != column)
    {
      if (!set) clearLED(row, column);
      else 
      {
        setLED(row, column);
        play(23-yGrid-row, column);
      }
    }

    if (!menuActivated && row >= 0 && row < 8 && column > -1 && column <= 8) LCDDisplayNoteInfo(row-(8-yGrid), column+xGrid+1);

    TSLastPressed[0] = row;
    TSLastPressed[1] = column;
  }
}
