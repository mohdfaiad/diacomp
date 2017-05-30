unit DiaryCore;

{ Data & Persistence }

interface

uses
  // ���������
  SysUtils,

  // ������
  BusinessObjects,
  DiaryDatabase,
  Bases,
  DiaryRoutines,
  DiaryDAO,
  DiaryLocalSource,
  DiaryWebSource,

  FoodBaseDAO,
  FoodBaseLocalDAO,
  FoodBaseWebDAO,

  DishBaseDAO,
  DishBaseLocalDAO,
  DishBaseWebDAO,

  {#}DiaryWeb,  
  AnalyzeInterface,
  DiaryAnalyze,
  AutoLog,
  SettingsINI,

  // ������
  InetDownload;

type
  TItemType = (itUnknown, itFood, itDish);
  TSyncResult = (srEqual, srFirstUpdated, srSecondUpdated);

  procedure Initialize();
  procedure Finalize();

  { load/save }
  procedure LoadExpander; deprecated;
  procedure SaveExpander; deprecated;

  function IdentifyItem(const ItemName: string; out Item: TVersioned): TItemType;

  { web }
  function DownloadFoodBaseSample: boolean;
  function DownloadDishBaseSample: boolean;

  function ExportKoofs(Plain: boolean): string;
  function GetKoof(Time: integer): TKoof;

var
  { ������ }
  LocalSource: TDiaryDAO;
  WebSource: TDiaryDAO;

  Diary: TDiary;

  WebClient: TDiacompClient;
  Expander: TStringMap;

  FoodBaseLocal: TFoodBaseDAO;
  FoodBaseWeb: TFoodBaseDAO;

  DishBaseLocal: TDishBaseDAO;
  DishBaseWeb: TDishBaseDAO;

  Analyzers: TAnalyzers;
  AnalyzeResults: TAnalyzeResults;
  AvgAnalyzeResult: TAnalyzeResult;

  { ��������� }
  Inited: boolean = False;
  WORK_FOLDER: string = ''; // ������� ����������
  AnExpDate: string; { ��� �������� ���� ����������, ����������� ��� ������ }

const
  { ��������� }
  ADVANCED_MODE                  = True;
  PROGRAM_VERSION                = '2.06';
  PROGRAM_VERSION_CODE : integer = 206;
  PROGRAM_DATE                   = '2017.05.30';
  UPDATES_CHECKING_PERIOD        = 1; { ���� }

  { ����� ������������ ������ � �������� }
  BLOOD_ACTUALITY_TIME  = 120;     // minutes
  INS_ACTUALITY_TIME    = 4 * 60;  // minutes
  SEARCH_INTERVAL       = 14;      // for time left

  { ��������� ����������� }
  MAX_FOODBASE_SIZE   = 500 * 1024; // byte
  MAX_DISHBASE_SIZE   = 500 * 1024; // byte

  { URL's }
  URL_SERVER          = 'http://diacomp.net/api/windows/';

  URL_UPDATE          = URL_SERVER + 'file/compensation.exe';
  URL_RESTART         = URL_SERVER + 'file/restart.exe';
  URL_FOODBASE_SAMPLE = URL_SERVER + 'file/demofoodbase.xml';
  URL_DISHBASE_SAMPLE = URL_SERVER + 'file/demodishbase.xml';

  { Local files }

  // TODO: refactor constants' names

  FOLDER_BASES        = 'Bases';

    FoodBase_Name     = 'FoodBase.xml';
    FoodBase_Name_old = 'FoodBase.txt';
    FoodBaseHash_Name = 'FoodBaseHash.txt';

    DishBase_Name     = 'DishBase.xml';
    DishBase_Name_old = 'DishBase.txt';
    DishBaseHash_Name = 'DishBaseHash.txt';

    Diary_Name        = 'Diary.txt';

    Expander_Name     = 'Expander.txt';

    FoodBase_FileName     = FOLDER_BASES + '\' + FoodBase_Name;
    FoodBaseHash_FileName = FOLDER_BASES + '\' + FoodBaseHash_Name;

    DishBase_FileName     = FOLDER_BASES + '\' + DishBase_Name;
    DishBaseHash_FileName = FOLDER_BASES + '\' + DishBaseHash_Name;

    Diary_FileName        = FOLDER_BASES + '\' + Diary_Name;

    Expander_FileName     = FOLDER_BASES + '\' + Expander_Name;

implementation

{======================================================================================================================}
procedure Initialize();
{======================================================================================================================}
begin
  AutoLog.Log(DEBUG, 'Loading local diary...');
  LocalSource := TDiaryLocalSource.Create(WORK_FOLDER + Diary_FileName);
  AutoLog.Log(DEBUG, 'Local diary loaded');

  WebClient := TDiacompClient.Create;
  WebSource := TDiaryWebSource.Create(WebClient);

  Diary := TDiary.Create(LocalSource);

  FoodBaseLocal := TFoodBaseLocalDAO.Create(WORK_FOLDER + FoodBase_FileName);
  FoodBaseWeb := TFoodbaseWebDAO.Create(WebClient);

  DishBaseLocal := TDishBaseLocalDAO.Create(WORK_FOLDER + DishBase_FileName);
  DishBaseWeb := TDishbaseWebDAO.Create(WebClient);

  Expander := TStringMap.Create;
end;

{======================================================================================================================}
procedure Finalize();
{======================================================================================================================}
begin
  Diary.Free; // before sources finalization
  LocalSource.Free;
  WebSource.Free; // before WebClient
  WebClient.Free;

  FoodBaseLocal.Free;
  FoodBaseWeb.Free;
  DishBaseLocal.Free;
  DishBaseWeb.Free;

  Expander.Free;
end;

{======================================================================================================================}
function GetKoof(Time: integer): TKoof;
{======================================================================================================================}
begin
  Result := AvgAnalyzeResult.KoofList[Time];
end;

{======================================================================================================================}
function DownloadDishBaseSample(): boolean;
{======================================================================================================================}
begin
  Result := GetInetFile(URL_DISHBASE_SAMPLE, WORK_FOLDER + DishBase_FileName, MAX_DISHBASE_SIZE);
end;

{======================================================================================================================}
function DownloadFoodBaseSample(): boolean;
{======================================================================================================================}
begin
  Result := GetInetFile(URL_FOODBASE_SAMPLE, WORK_FOLDER + FoodBase_FileName, MAX_FOODBASE_SIZE);
end;

{======================================================================================================================}
function IdentifyItem(const ItemName: string; out Item: TVersioned): TItemType;
{======================================================================================================================}
begin
  Item := FoodBaseLocal.FindOne(ItemName);
  if (Item <> nil) then
  begin
    Result := itFood;
    Exit;
  end;

  Item := DishBaseLocal.FindOne(ItemName);
  if (Item <> nil) then
  begin
    Result := itDish;
    Exit;
  end;

  Result := itUnknown;
end;

{======================================================================================================================}
procedure LoadExpander;
{======================================================================================================================}
begin
  if FileExists(WORK_FOLDER + Expander_FileName) then
    Expander.LoadFromFile(WORK_FOLDER + Expander_FileName);
end;

{======================================================================================================================}
procedure SaveExpander;
{======================================================================================================================}
begin
  Expander.SaveToFile(WORK_FOLDER + Expander_FileName);
end;

{======================================================================================================================}
function ExportKoofs(Plain: boolean): string;
{======================================================================================================================}
var
  i: integer;
  s: string;
  DC: char;
  Koof: TKoof;
begin
  StartProc('ExportKoofs()');

  Result := '';
  if (Plain) then
  begin
    for i := 0 to MinPerDay - 1 do
    begin
      Koof := GetKoof(i);
      Result := Result +
        //Format('%2.2d',[i]) + '.00 - '+Format('%2.2d',[i + 1]) + '.00' + #9+
        Format('%.6f'#9'%.6f'#9'%.6f'#13#10, [Koof.k, Koof.q, Koof.p])
      ;
    end;
  end else
  begin
    Result := '[';
    DC := DecimalSeparator;
    try                    
      DecimalSeparator := '.';

      for i := 0 to MinPerDay - 1 do
      begin
        Koof := GetKoof(i);

        s := Format('{"time":%d,"k":%.4f,"q":%.2f,"p":%.2f}',[i, Koof.k, Koof.q, Koof.p]);
        if (i < MinPerDay - 1) then
          s := s + ',';
        Result := Result + s;
      end;
    finally
      DecimalSeparator := DC;
    end;  
    Result := Result + ']';
  end;

  FinishProc;
end;

end.
