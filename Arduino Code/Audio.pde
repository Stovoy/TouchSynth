void audioInit()
{
  computeAllFrequencies();
  for (int i = 0; i < 48; ++i)
    localSettings[i] = B00001100;
}

byte notesList[24];
long notesAmount;
int lastAudioValue = 0;

void checkGrid()
{
  notesAmount = 0;
  for (int i = 0; i < 24; ++i)
    if (checkLED_Matrix(i, playColumn))
      notesList[notesAmount++] = 23-i;
}

void computeAllFrequencies()
{
  for (int i = 0; i < 24; ++i)
    noteSize[i] = (11000/notes[i])+1;
}

byte addNote(int noteNum)
{
  byte tempBufferByte = pgm_read_byte_near(*waveform + pgmLoc[noteNum] + positions[noteNum]);
  if (++positions[noteNum] > noteSize[noteNum]-2) positions[noteNum] = 0;
  return tempBufferByte;
}

byte addNotes()
{
  word tempBuffer = 0;
  for (int i = notesAmount-1; i >= 0; --i)
  {
    tempBuffer += pgm_read_byte_near(*waveform + pgmLoc[notesList[i]] + positions[i]);
    if (++positions[i] > noteSize[notesList[i]]-2) positions[i] = 0;
  }
  return tempBuffer/notesAmount;
}

void play()
{
  noteComplete = false;
  for (int i = 0; i < 24; ++i) positions[i] = 0;
  checkGrid();
  unsigned long startTime;
  byte value;
  noInterrupts();
  int column = playColumn;
  while (column > 7) column -= 8;
  column = 7 - column;
  int row = -1;
  int count = 0;
  int distance = numColumnSets*16-8;
  int length;
  if (fur)
  {
    length = 16;
    if (playColumn >= 252) length = 8;
    if (playColumn >= 407) length = 16;
    if (playColumn >= 698) length = 12;
  }
  else
  {
    if ((localSettings[playColumn/16] & B00000001) == 0) 
      length = 16-((globalSettings & B00011110) >> 1);
    else
      length = 16-((localSettings[playColumn/16] & B00011110) >> 1);
  }
  for (int i = 0; i < length*100; ++i)
  {
    startTime = micros();
    value = addNotes();
    PORTA = 0;
    PORTL = ~_BV(row < 7 ? ++row : row = 0);
    PORTA = count == 0 ? selectedMatrix[row] : selectedMatrix[row] & _BV(playColumn < distance ? 7 : 7 - (playColumn - xGrid));
    if (row == 0 && count++ > 5) count = 0;
    while (micros() - startTime < 96);
    PORTC = value;
    lastAudioValue = value;
  }
  PORTA = 0;
  PORTL = 255;
  interrupts();
  if (++playColumn > numColumnSets*16-1)
  {
    playColumn = 0;
    xGrid = 0;
  }
  else if (playColumn < distance+1) ++xGrid;
  setSelectedMatrix();
  noteComplete = true;
}

void play(int noteNum, int column)
{
  for (int i = 0; i < 24; ++i) positions[i] = 0;
  unsigned long startTime;
  byte value;
  noInterrupts();
  int portL = 23-yGrid-noteNum, portA = 7-column;
  int tempo = ((globalSettings & B00011110) >> 1);
  for (int i = 0; i < tempo*150; ++i)
  {
    startTime = micros();
    value = addNote(noteNum);
    PORTL = ~_BV(portL);
    PORTA = _BV(portA);
    while (micros() - startTime < 96);
    PORTC = value;
  }
  interrupts();
}
