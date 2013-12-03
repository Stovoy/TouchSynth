void checkButtonState()
{
  buttonState[0] = !digitalRead(17); //U
  buttonState[1] = !digitalRead(19); //D
  buttonState[2] = !digitalRead(20); //L
  buttonState[3] = !digitalRead(21); //R
  buttonState[4] = digitalRead(16); //Enter
  buttonState[5] = !digitalRead(7); //Play

  if (buttonState[0] && lastRead[0] != buttonState[0]) u();
  if (buttonState[1] && lastRead[1] != buttonState[1]) d();
  if (buttonState[2] && lastRead[2] != buttonState[2]) l();
  if (buttonState[3] && lastRead[3] != buttonState[3]) r();
  if (buttonState[4] && lastRead[4] != buttonState[4]) enter();
  if (buttonState[5] && noteComplete)
  {
    if (!playing)
    {
      storedXGrid = xGrid;
      playColumn = xGrid;
      MsTimer2::stop();
    }
    playing = true;
    play();
  }
  else if (!buttonState[5] && playing) 
  {
    xGrid = storedXGrid;
    playing = false;
    MsTimer2::set(1, update);
    MsTimer2::start();
    setSelectedMatrix();
  }
  for (int i = 0; i < 5; ++i)
    lastRead[i] = buttonState[i];
}

void u()
{
  if (millis() - buttonTimeClicked[0] <= debouncing || (TSScroll && (millis() - buttonTimeClicked[0] <= TSScrollTime)))
    return;
  buttonTimeClicked[0] = millis();
  if (buttonState[0])
  {
    if (menuActivated) menuUp();
    else scroll(0);
  }
}

void d()
{
  if (millis() - buttonTimeClicked[1] <= debouncing || (TSScroll && (millis() - buttonTimeClicked[1] <= TSScrollTime)))
    return;
  buttonTimeClicked[1] = millis();
  if (buttonState[1])
  {
    if (menuActivated) menuDown();
    else scroll(1);
  }
}

void l()
{
  if (millis() - buttonTimeClicked[2] <= debouncing || (TSScroll && (millis() - buttonTimeClicked[2] <= TSScrollTime)))
    return;
  buttonTimeClicked[2] = millis();
  if (buttonState[2])
  {
    if (menuActivated) menuOut();
    else scroll(2);
  }
}

void r()
{
  if (millis() - buttonTimeClicked[3] <= debouncing || (TSScroll && (millis() - buttonTimeClicked[3] <= TSScrollTime)))
    return;
  buttonTimeClicked[3] = millis();
  if (buttonState[3])
  {
    if (menuActivated) menuIn();
    else scroll(3);
  }
}

void enter()
{
  if (millis() - buttonTimeClicked[4] <= debouncing)
    return;
  buttonTimeClicked[4] = millis();
  if (buttonState[4])
  { 
    if (!menuActivated) menuActivated = true;
    else
    {
      if (menuPos[0] == 1)
      {
        if (menuDepth != 2) ++menuDepth;
        else if ((menuPos[1] == 0 && menuPos[2] == 2) || (menuPos[1] == 1 && menuPos[2] == 1)) 
        {
          menuPos[2] = 0;
          --menuDepth;
        }
      }
      if (menuPos[0] > 1 && menuPos[0] < 8 && menuPos[0] != 5)
      {
        if (menuPos[0] == 2) save();
        else if (menuPos[0] == 3) load();
        else if (menuPos[0] == 4) reset();
        else if (menuPos[0] == 6) loadFurElise();
        else if (menuPos[0] == 7) loadStepeggio();
        
        menuActivated = false;
        menuPos[0] = 0;
        LCDDisplayTitle();
        return;
      }
      else if (menuDepth == 1 && state2[menuPos[1]] < 2)
      {
          if (menuPos[1] == 0) waveform = &sines;
          else if (menuPos[1] == 1) waveform = &triangles;  
          else if (menuPos[1] == 2) waveform = &saws;
          else if (menuPos[1] == 3) waveform = &squares;
      }
      else if (menuDepth == 2 && menuPos[1] == 0 && menuPos[2] == 1) localSettings[xGrid/16] ^= 1;
    }
    menuDisplay();
  }
}

