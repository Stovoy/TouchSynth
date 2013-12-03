void matrixInit()
{
  xGrid = 0;
  yGrid = 16;

  for (int i = 0; i < 24; ++i)
    for (int j = 0; j < 48; ++j)
      //for (int k = 0; k < 1; ++k)
        matrix[i][j] = 0;
  setSelectedMatrix();
}

void setSelectedMatrix()
{
  for (int row = 0; row < 8; ++row)
  {
    int currentWord = xGrid/16;
    if (xGrid % 16 == 0)
      selectedMatrix[row] = matrix[row+yGrid][currentWord] >> 8;
    else
    {
      int relativePosition = xGrid;
      while (relativePosition > 15)
        relativePosition -= 16;
      byte one, two;
      if (relativePosition > 8)
      {
        one = (matrix[row+yGrid][currentWord] << relativePosition-8) & ~(255 >> 16-relativePosition);
        two = (matrix[row+yGrid][currentWord+1] >> 24-relativePosition) & (255 >> 16-relativePosition);
        selectedMatrix[row] = one | two;	
      }
      else
      {
        selectedMatrix[row] = matrix[row+yGrid][currentWord] >> 8-relativePosition;
      }
    }
  }
}

void setGrid(int number)
{
  grid = number;
  setSelectedMatrix();
}

void update()
{
  if (timerRunning)
    if (++timer >= timerAmount) 
      timerFunction();
  PORTA = 0;
  PORTC = lastAudioValue;
  PORTL = ~(_BV(row));
  PORTA = selectedMatrix[row];
  if (++row > 7) row = 0;
}

void setLED(int row, int column)
{
  int relativePosition = xGrid;
  while (relativePosition > 15)
    relativePosition -= 16;
  int position; 
  if (relativePosition <= 8 || xGrid/16 == (xGrid+column)/16)
    position = 15-relativePosition-column;
  else
    position = 31-relativePosition-column;
  matrix[yGrid + row][(xGrid + column)/16] |= 1 << position;
  setSelectedMatrix();
}

void clearLED(int row, int column)
{
  int relativePosition = xGrid;
  while (relativePosition > 15)
    relativePosition -= 16;
  int position; 
  if (relativePosition <= 8 || xGrid/16 == (xGrid+column)/16)
    position = 15-relativePosition-column;
  else
    position = 31-relativePosition-column;
  matrix[yGrid + row][(xGrid + column)/16] &= ~(1 << position);
  setSelectedMatrix();
}

boolean checkLED_Selected(int row, int column)
{
  int relativePosition = xGrid;
  while (relativePosition > 15)
    relativePosition -= 16;

  int position; 
  if (relativePosition <= 8 || xGrid/16 == (xGrid+column)/16)
    position = 15-relativePosition-column;
  else
    position = 31-relativePosition-column;  
  return (matrix[yGrid + row][(xGrid + column)/16] & (1 << position)) >> position;
}

boolean checkLED_Matrix(int row, int column)
{
  int relativePosition = column;
  while (relativePosition > 15)
    relativePosition -= 16;

  int position = 15-relativePosition;
  return (matrix[row][column/16] & (1 << position)) >> position;
}

void scroll(short button)
{
  switch(button)
  {
  case 0:
    if (yGrid != 0) --yGrid;
    break;
  case 1:
    if (yGrid < 16) ++yGrid;
    break;
  case 2:
    if (xGrid != 0) --xGrid;
    break;
  case 3:
    if (xGrid < numColumnSets*16-8) ++xGrid;
    break;
  }
  setSelectedMatrix();
  TSScroll = false;
}
