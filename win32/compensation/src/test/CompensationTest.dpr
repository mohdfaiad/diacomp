program CompensationTest;

uses
  Forms,
  TestFrameWork,
  GUITestRunner,
  BusinessObjectsTest in 'BusinessObjectsTest.pas',
  BusinessObjects in '..\bo\BusinessObjects.pas',
  DiaryRoutines in '..\common\DiaryRoutines.pas',
  BasesTest in 'BasesTest.pas',
  Bases in '..\bo\Bases.pas',
  AutoLog in '..\common\AutoLog.pas',
  DiaryRoutinesTest in 'DiaryRoutinesTest.pas',
  DiaryWeb in '..\common\DiaryWeb.pas',
  DiarySources in '..\common\DiarySources.pas',
  DiaryPage in '..\bo\DiaryPage.pas',
  DiaryRecords in '..\bo\DiaryRecords.pas',
  DiaryPageSerializer in '..\common\DiaryPageSerializer.pas';

{$R *.res}

begin
  Application.Initialize;
  GUITestRunner.RunRegisteredTests;
end.
