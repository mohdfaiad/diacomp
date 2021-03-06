unit DiaryLocalSource;

interface

uses
  Windows,
  SysUtils, // FileExists(), Now()
  Classes,
  Dialogs {update warnings},
  DiaryRoutines,
  DiaryDAO,
  DiaryPage,
  DiaryPageSerializer,
  DiaryRecords,
  uLkJSON;

type
  TPageData = class;
  TPageDataList = array of TPageData;

  // �������� ������������ ��������
  TPageData = class
  private
    procedure CopyFrom(ADate: TDate; ATimeStamp: TDateTime; AVersion: integer; const APage: string); overload;
    procedure CopyFrom(APage: TPageData); overload;
  public
    Date: TDate;
    TimeStamp: TDateTime;
    Version: integer;
    Page: string;

    constructor Create(ADate: TDate; ATimeStamp: TDateTime; AVersion: integer; const APage: string); overload;
    constructor Create(APage: TPageData); overload;

    function Write(F: TFormatSettings): string; overload;
    function WriteHeader(F: TFormatSettings): string;

    class procedure Read(const S: string; Page: TPageData; F: TFormatSettings); overload;
    {L} class procedure Read(S: TStrings; F: TFormatSettings; out Pages: TPageDataList); overload;
    {L} class procedure Read(PageData: TPageData; Page: TDiaryPage); overload;
    class procedure ReadHeader(const S: string; PageData: TPageData; F: TFormatSettings); overload;
    {L} class procedure Write(Page: TDiaryPage; PageData: TPageData); overload;
    {L} class procedure Write(const Pages: TPageDataList; S: TStrings; F: TFormatSettings); overload;
  end;

  TDiaryLocalSource = class (TDiaryDAO)
  private
    FPages: TPageDataList;
    FModified: boolean;
    FFileName: string;

    function Add(Page: TPageData): integer;
    procedure Clear;
    function GetPageIndex(Date: TDate): integer;
    function TraceLastPage: integer;

    procedure LoadFromFile(const FileName: string);
    procedure SaveToFile(const FileName: string);
    procedure MigrateToV4(const FileName: string);
  public
    constructor Create(const FileName: string);
    destructor Destroy; override;

    procedure GetModified(Time: TDateTime; out ModList: TModList); override;
    procedure GetVersions(const Dates: TDateList; out ModList: TModList); override;
    function GetPages(const Dates: TDateList; out Pages: TDiaryPageList): boolean; override;
    function PostPages(const Pages: TDiaryPageList): boolean; override;

    // ��������
    // TODO: think about it
    //property Modified: boolean read FModified {write FModified};
 end;

implementation

var
  LocalFmt: TFormatSettings;
  
const
  ShowUpdateWarning = True;

{ TPageData }

{==============================================================================}
procedure TPageData.CopyFrom(ADate: TDate; ATimeStamp: TDateTime; AVersion: integer; const APage: string);
{==============================================================================}
begin
  Date := ADate;
  TimeStamp := ATimeStamp;
  Version := AVersion;
  Page := APage;
end;

{==============================================================================}
procedure TPageData.CopyFrom(APage: TPageData);
{==============================================================================}
begin
  CopyFrom(APage.Date, APage.TimeStamp, APage.Version, APage.Page);
end;

{==============================================================================}
constructor TPageData.Create(ADate: TDate; ATimeStamp: TDateTime;
  AVersion: integer; const APage: string);
{==============================================================================}
begin
  CopyFrom(ADate, ATimeStamp, AVersion, APage);
end;

{==============================================================================}
constructor TPageData.Create(APage: TPageData);
{==============================================================================}
begin
  CopyFrom(APage);
end;

{==============================================================================}
function TPageData.Write(F: TFormatSettings): string;
{==============================================================================}
var
  Header: string;
  Body: string;
begin
  Header := WriteHeader(F);
  Body := Page;
  Result := Header + #13 + Body;
end;

{==============================================================================}
function TPageData.WriteHeader(F: TFormatSettings): string;
{==============================================================================}
begin
  TPageSerializer.WriteHeader(Date, Timestamp, Version, F, Result);
end;

{==============================================================================}
class procedure TPageData.Read(const S: string; Page: TPageData; F: TFormatSettings);
{==============================================================================}
var
  header, body: string;
begin
  Separate(S, header, #13, body);
  TPageData.ReadHeader(header, Page, F);
  Page.Page := body;
end;

{==============================================================================}
class procedure TPageData.Read(PageData: TPageData; Page: TDiaryPage);
{==============================================================================}
begin
  with Page do
  begin
    Date := PageData.Date;
    TimeStamp := PageData.TimeStamp;
    Version := PageData.Version;
    TPageSerializer.ReadBody(PageData.Page, Page);
  end;


  {

  Switch: boolean;

  ------

  Switch := Switch and (not SilentMode);
  SilentMode := SilentMode or Switch;

  ...

  SilentMode := SilentMode and (not Switch);

  ------

  OldMode := SilentMode;
  if Switch then SilentMode := True;

  ...

  SilentMode := OldMode;

  }
end;

{==============================================================================}
class procedure TPageData.ReadHeader(const S: string; PageData: TPageData; F: TFormatSettings);
{==============================================================================}
begin
  TPageSerializer.ReadHeader(S, F, PageData.Date, PageData.TimeStamp, PageData.Version);
end;

{==============================================================================}
class procedure TPageData.Read(S: TStrings; F: TFormatSettings; out Pages: TPageDataList);
{==============================================================================}
var
  PageList: TStringsArray;
  i: integer;
begin
  TPageSerializer.SeparatePages(S, PageList);

  SetLength(Pages, Length(PageList));
  for i := 0 to High(PageList) do
  begin
    Pages[i] := TPageData.Create;
    TPageData.Read(PageList[i].Text, Pages[i], F);
  end;
end;

{==============================================================================}
class procedure TPageData.Write(Page: TDiaryPage; PageData: TPageData);
{==============================================================================}
begin
  with Page do
  begin
    PageData.Date := Date;
    PageData.TimeStamp := TimeStamp;
    PageData.Version := Version;
    TPageSerializer.WriteBody(Page, PageData.Page);
  end;
end;

{==============================================================================}
class procedure TPageData.Write(const Pages: TPageDataList; S: TStrings; F: TFormatSettings);
{==============================================================================}
var
  i: integer;
begin
  // ������ �������� ����� ���������� ����� ������������ ������� �� ���������,
  // ������� ��������� ������ �������� � �������� - �������! :-)
  // ---
  // No. � ����� �������� � �� ����� ������������ �������� �� ������. ������ ���������� - ���� ����������
  // ---
  /// Yes. ��������� ����� ����� ������, � �� ����������.

  for i := 0 to High(Pages) do
  // (Trim(Pages[i].Page) <> '') then
  if (Pages[i].Version > 0) then
      s.Add(Pages[i].Write(F));
end;

{ TDiaryLocalSource }

{==============================================================================}
function TDiaryLocalSource.Add(Page: TPageData): integer;
{==============================================================================}
begin
  // 1. �������� ������
  // 2. ����������

  Result := GetPageIndex(Page.Date);
  if (Result = -1) then
  begin
    Result := Length(FPages);
    SetLength(FPages, Result + 1);
    FPages[Result] := Page;
    Result := TraceLastPage();
  end else
  if (Page <> FPages[Result]) then
  begin
    FPages[Result].Free;
    FPages[Result] := Page;
  end;
end;

{==============================================================================}
procedure TDiaryLocalSource.Clear;
{==============================================================================}
var
  i: integer;
begin
  { ���������� � ����������� � ����� ��������� �� ����� }

  if (Length(FPages) > 0) then
    FModified := True;

  for i := 0 to High(FPages) do
    FPages[i].Free;
  SetLength(FPages, 0);
end;

{==============================================================================}
constructor TDiaryLocalSource.Create(const FileName: string);
{==============================================================================}
begin
  FModified := False;
  FFileName := FileName;

  if (FileExists(FileName)) then
    LoadFromFile(FileName);
end;

{==============================================================================}
destructor TDiaryLocalSource.Destroy;
{==============================================================================}
begin
  Clear;
  inherited;
end;

{==============================================================================}
procedure TDiaryLocalSource.GetModified(Time: TDateTime; out ModList: TModList);
{==============================================================================}
var
  i, Count: integer;
begin
  Count := 0;
  SetLength(ModList, 1);
  for i := 0 to High(FPages) do
  if (FPages[i].TimeStamp > Time) {and (FPages[i].Version > 0)} then
  begin
    if (Count = Length(ModList)) then
      SetLength(ModList, Length(ModList) * 2);
    ModList[Count].Date := FPages[i].Date;
    ModList[Count].Version := FPages[i].Version;
    inc(Count);
  end;
  SetLength(ModList, Count);
end;

{==============================================================================}
procedure TDiaryLocalSource.GetVersions(const Dates: TDateList; out ModList: TModList);
{==============================================================================}
var
  i, k: integer;
begin
  SetLength(ModList, Length(Dates));
  for i := 0 to High(Dates) do
  begin
    ModList[i].Date := Dates[i];

    k := GetPageIndex(Dates[i]);
    if (k > -1) then
      ModList[i].Version := FPages[k].Version
    else
      ModList[i].Version := 0;
  end;
end;

{==============================================================================}
function TDiaryLocalSource.GetPageIndex(Date: TDate): integer;
{==============================================================================}
var
  L,R: integer;
begin
  L := 0;
  R := High(FPages);
  while (L <= R) do
  begin
    Result := (L + R) div 2;
    if (FPages[Result].Date < Date) then L := Result + 1 else
    if (FPages[Result].Date > Date) then R := Result - 1 else
      Exit;
  end;
  Result := -1;
end;

{==============================================================================}
function TDiaryLocalSource.GetPages(const Dates: TDateList; out Pages: TDiaryPageList): boolean;
{==============================================================================}
var
  i, Index: integer;
begin
  SetLength(Pages, Length(Dates));
  for i := 0 to High(Dates) do
  begin
    Index := GetPageIndex(Dates[i]);
    if (Index > -1) then
    begin
      Pages[i] := TDiaryPage.Create;
      TPageData.Read(FPages[Index], Pages[i]);
    end else
    begin
      Pages[i] := TDiaryPage.Create;
      Pages[i].Date := Dates[i];
      Pages[i].TimeStamp := 0; //GetTimeUTC();
    end;
  end;
  Result := True;
end;

{==============================================================================}
procedure TDiaryLocalSource.LoadFromFile(const FileName: string);
{==============================================================================}

  procedure Load_v1(S: TStrings);
  var
    Pages: TPageDataList;
    i: integer;
  begin
    s.Delete(0);
    s.Delete(0);
    TPageData.Read(S, LocalFmt, Pages);

    // ��� �������� ���������� � ������ ���������� ��������������� ������
    for i := 0 to High(Pages) do
      Add(Pages[i]);
  end;

  procedure Load_v2(S: TStrings);
  var
    Pages: TPageDataList;
    i: integer;
  begin
    TPageData.Read(S, LocalFmt, Pages);
    for i := 0 to High(Pages) do
      Pages[i].TimeStamp := LocalToUTC(Pages[i].TimeStamp);

    // ��� �������� ���������� � ������ ���������� ��������������� ������
    for i := 0 to High(Pages) do
      Add(Pages[i]);
  end;

  procedure Load_v3(S: TStrings);
  var
    Pages: TPageDataList;
    i: integer;
  begin
    s.Delete(0);
    TPageData.Read(S, LocalFmt, Pages);

    // ��� �������� ���������� � ������ ���������� ��������������� ������
    for i := 0 to High(Pages) do
      Add(Pages[i]);
  end;

var
  s: TStrings;
  BaseVersion: integer;
begin
  Clear;

  s := TStringList.Create;

  try
    s.LoadFromFile(FileName);

    // ����� ������ ������
    if (s.Count >= 2) and (s[0] = 'DIARYFMT') then
    begin
      Load_v1(s);
    end else

    // ������ � ��������� ������
    if (s.Count >= 1) and (pos('VERSION=', s[0]) = 1) then
    begin
      BaseVersion := StrToInt(TextAfter(s[0], '='));
      case BaseVersion of
        3:    Load_v3(s);
        else raise Exception.Create('Unsupported database format');
      end;
    end else

    // ������ ��� ������ - �����������
    begin
      Load_v2(s);
    end;

  finally
    s.Free;
  end;

  FModified := False;  // ��)

  MigrateToV4('Bases\Diary_migrated_v4.txt');
end;

{==============================================================================}
function TDiaryLocalSource.PostPages(const Pages: TDiaryPageList): boolean;
{==============================================================================}

  function PureLength(const S: string): integer;
  var
    i: integer;
  begin
    Result := Length(S);
    for i := 1 to Length(S) do
      if (S[i] = #10) or (S[i] = #13) then
        dec(Result);
  end;

  function Worry(PageOld, PageNew: TPageData): boolean;
  var
    Msg: string;
    MsgType: TMsgDlgType;
  begin
    Msg := '';
    MsgType := mtConfirmation;

    if (PageOld.Version > PageNew.Version) then
    begin
      MsgType := mtWarning;
      Msg := Msg + '* ������: ' + IntToStr(PageOld.Version) + ' --> ' + IntToStr(PageNew.Version) + #13;
    end;

    if (PageOld.TimeStamp > PageNew.TimeStamp) then
      Msg := Msg + '* �����: ' + DateTimeToStr(PageOld.TimeStamp) + ' --> ' + DateTimeToStr(PageNew.TimeStamp) + #13;

    if (PureLength(PageNew.Page) - PureLength(PageOld.Page) < -20) then
      Msg := Msg + '* ������: ' + IntToStr(Length(PageOld.Page)) + ' --> ' + IntToStr(Length(PageNew.Page)) + #13;

    Result :=
      (Msg <> '') and
      (MessageDlg(
        '�������� ' + DateToStr(PageOld.Date)+' ����� ������������.'+#13+
        '���������� ����������:' + #13 +
        Msg + #13+
        '����������?',
         MsgType, [mbYes,mbNo],0) <> 6); // mrYes
  end;

var
  i, Index: integer;
  PageData: TPageData;
begin
  for i := 0 to High(Pages) do
  begin
    // �����������
    PageData := TPageData.Create();
    TPageData.Write(Pages[i], PageData);

    // ����
    Index := GetPageIndex(Pages[i].Date);
    if (Index = -1) then
    begin
      SetLength(FPages, Length(FPages) + 1);
      FPages[High(FPages)] := PageData;
      TraceLastPage();
    end else
    begin   
      if (not ShowUpdateWarning) or (not Worry(FPages[Index], PageData)) then
      begin
        FPages[Index].Free;
        FPages[Index] := PageData;
      end;
    end;
  end;
  if (Length(Pages) > 0) then
  begin
    FModified := True;
    SaveToFile(FFileName);
  end;

  Result := True;
end;

{==============================================================================}
procedure TDiaryLocalSource.SaveToFile(const FileName: string);
{==============================================================================}
var
  s: TStringList;
begin
  s := TStringList.Create;
  try
    S.Add('VERSION=3');
    TPageData.Write(FPages, S, LocalFmt);
    s.SaveToFile(FileName);
  finally
    s.Free;
  end;
  FModified := False;
end;

procedure TDiaryLocalSource.MigrateToV4(const FileName: string);

  function SerializeData(Rec: TCustomRecord): string;
  var
    Json: TlkJSONobject;
  begin
    try
      Json := DiaryPageSerializer.SerializeDiaryRecord(Rec);
      Result := TlkJSON.GenerateText(Json);
    finally
      Json.Free;
    end;
  end;

  function Serialize(Rec: TCustomRecord): string;
  begin
    Rec.TimeStamp := Rec.GetNativeTime;
                        
    Result := Format('%s'#9'%s'#9'%s'#9'%d'#9'%s'#9'%s',
      [
        DateTimeToStr(Rec.GetNativeTime, STD_DATETIME_FMT),
        DateTimeToStr(Rec.TimeStamp, STD_DATETIME_FMT),
        //CreateCompactGuid(), //Rec.Hash,
        CreateCompactGuid(), //Rec.ID,
        1, //Rec.Version,
        WriteBoolean(false), // WriteBoolean(Deleted),
        SerializeData(Rec)
      ]
    );
  end;

var
  DataLine: string;
  i, j: integer;
  S: TStrings;

  Page: TDiaryPage;
  Rec: TCustomRecord;
begin
  S := TStringList.Create;
  try
    s.Add('VERSION=' + IntToStr(4));

    for i := 0 to High(FPages) do
    begin
      Page := TDiaryPage.Create;
      TPageData.Read(FPages[i], Page);
      for j := 0 to Page.Count - 1 do
      begin
        Rec := Page[j];
        DataLine := Serialize(Rec);
        S.Add(DataLine);
      end;
    end;

    S.SaveToFile(FileName);
  finally
    S.Free;
  end;
end;

{==============================================================================}
function TDiaryLocalSource.TraceLastPage: integer;
{==============================================================================}
var
  Temp: TPageData;
begin
  Result := High(FPages);
  if (Result > -1) then
  begin
    Temp := FPages[Result];
    while (Result > 0) and (FPages[Result-1].Date > Temp.Date) do
    begin
      FPages[Result] := FPages[Result-1];
      dec(Result);
    end;
    FPages[Result] := Temp;
  end;
end;

initialization
  // 02.04.1992 09:45:00
  GetLocaleFormatSettings(GetThreadLocale, LocalFmt);
  LocalFmt.DateSeparator := '.';
  LocalFmt.TimeSeparator := ':';
  LocalFmt.ShortDateFormat := 'dd.mm.yyyy';
  LocalFmt.LongTimeFormat := 'hh:nn:ss';
end.
