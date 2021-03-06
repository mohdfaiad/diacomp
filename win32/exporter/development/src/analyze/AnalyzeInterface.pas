unit AnalyzeInterface;

interface

uses
  DiaryRoutines;

{
  ***** ������� ������� *****
  1) ��������� �� �������� ��������� ������� (TPrimeRec)
  2) ��������� �������������� � ��� TAnalyzeRec
  3) ����������� �� � ������ ������� � ��������� ����������� ������ �������������
}

type
  { ===== ������� ������ ===== }

  TAnalyzeRec = record
    Prots,Fats,Carbs: real;
    Ins: real;
    BSIn, BSOut: real;
    Time: integer; // ����� �������� (� �������)
    Weight: real; // ��� �����������
  end;

  TAnalyzeRecList = array of TAnalyzeRec;

  { ===== �������� ������ ===== }

  TKoof = record
    k,q,p: real;
  end;

  TKoofList = array[0..MinPerDay - 1] of TKoof;

  { ===== ������� ===== }

  TCallbackProgressProc = procedure(Progress: integer);
  TInfoFunction = function(): PChar;
  TAnalyzeFunction = function(const RecList: TAnalyzeRecList; var KoofList: TKoofList; CallBack: TCallbackProgressProc): boolean; StdCall;

const
  AnalyzeFunctionName = 'Analyze';
  InfoFunctionName    = 'Info';
  
implementation

end.
