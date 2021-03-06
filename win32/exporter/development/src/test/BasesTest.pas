unit BasesTest;

interface

uses
  TestFrameWork,
  SysUtils,
  BusinessObjects,
  Bases;

type
  TFoodBaseTest = class(TTestCase)
  private
    FoodBase: TFoodBase;
  protected
    procedure SetUp; override;
    procedure TearDown; override;
  published
    procedure TestAdd;
    procedure TestAddNil;
    procedure TestDelete;
  end;

  TDishBaseTest = class(TTestCase)
  private
    DishBase: TDishBase;
  protected
    procedure SetUp; override;
    procedure TearDown; override;
  published
    procedure TestAdd;
    procedure TestAddNil;
    procedure TestDelete;
  end;

implementation

{ TFoodBaseTest }

procedure TFoodBaseTest.SetUp;
begin
  inherited;
  FoodBase := TFoodBase.Create();
end;

procedure TFoodBaseTest.TearDown;
begin
  inherited;
  FoodBase.Free;
end;

procedure TFoodBaseTest.TestAdd;
var
  FoodBase: TFoodBase;
  Food: TFood;
  Index: integer;
begin
  FoodBase := TFoodBase.Create();

  Food := TFood.Create;
  Food.Name := '������';
  Index := FoodBase.Add(Food);
  CheckEquals(0, Index);
  CheckEquals(1, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '��������';
  Index := FoodBase.Add(Food);
  CheckEquals(0, Index);
  CheckEquals(2, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '�������';
  Index := FoodBase.Add(Food);
  CheckEquals(0, Index);
  CheckEquals(3, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '����';
  Index := FoodBase.Add(Food);
  CheckEquals(3, Index);
  CheckEquals(4, FoodBase.Version);

  CheckEquals(4, FoodBase.Count, 'Count check failed');

  CheckEqualsString('�������', FoodBase[0].Name);
  CheckEqualsString('��������', FoodBase[1].Name);
  CheckEqualsString('������', FoodBase[2].Name);
  CheckEqualsString('����', FoodBase[3].Name);

  FoodBase.Free;
end;

procedure TFoodBaseTest.TestAddNil;
begin
  ExpectedException := EInvalidPointer;
  FoodBase.Add(nil);
  ExpectedException := nil;
end;

procedure TFoodBaseTest.TestDelete;
var
  FoodBase: TFoodBase;
  Food: TFood;
begin
  FoodBase := TFoodBase.Create();

  Food := TFood.Create;
  Food.Name := '������';
  FoodBase.Add(Food);
  CheckEquals(1, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '��������';
  FoodBase.Add(Food);
  CheckEquals(2, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '�������';
  FoodBase.Add(Food);
  CheckEquals(3, FoodBase.Version);

  Food := TFood.Create;
  Food.Name := '����';
  FoodBase.Add(Food);
  CheckEquals(4, FoodBase.Version);

  CheckEquals(4, FoodBase.Count, 'Count check failed');

  FoodBase.Delete(0);
  CheckEquals(5, FoodBase.Version);

  CheckEquals(3, FoodBase.Count, 'Count check failed');

  CheckEqualsString('��������', FoodBase[0].Name);
  CheckEqualsString('������', FoodBase[1].Name);
  CheckEqualsString('����', FoodBase[2].Name);

  FoodBase.Free;
end;

{ TDishBaseTest }

procedure TDishBaseTest.SetUp;
begin
  inherited;
  DishBase := TDishBase.Create();
end;

procedure TDishBaseTest.TearDown;
begin
  inherited;
  DishBase.Free;
end;

procedure TDishBaseTest.TestAdd;
var
  DishBase: TDishBase;
  Dish: TDish;
  Index: integer;
begin
  DishBase := TDishBase.Create();

  Dish := TDish.Create;
  Dish.Name := '�������� �����';
  Index := DishBase.Add(Dish);
  CheckEquals(0, Index);
  CheckEquals(1, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '��������';
  Index := DishBase.Add(Dish);
  CheckEquals(0, Index);
  CheckEquals(2, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '��������';
  Index := DishBase.Add(Dish);
  CheckEquals(0, Index);
  CheckEquals(3, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '�������';
  Index := DishBase.Add(Dish);
  CheckEquals(3, Index);
  CheckEquals(4, DishBase.Version);

  CheckEquals(4, DishBase.Count, 'Count check failed');

  CheckEqualsString('��������', DishBase[0].Name);
  CheckEqualsString('��������', DishBase[1].Name);
  CheckEqualsString('�������� �����', DishBase[2].Name);
  CheckEqualsString('�������', DishBase[3].Name);

  DishBase.Free;
end;

procedure TDishBaseTest.TestAddNil;
begin
  ExpectedException := EInvalidPointer;
  DishBase.Add(nil);
  ExpectedException := nil;
end;

procedure TDishBaseTest.TestDelete;
var
  DishBase: TDishBase;
  Dish: TDish;
begin
  DishBase := TDishBase.Create();

  Dish := TDish.Create;
  Dish.Name := '�������� �����';
  DishBase.Add(Dish);
  CheckEquals(1, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '��������';
  DishBase.Add(Dish);
  CheckEquals(2, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '��������';
  DishBase.Add(Dish);
  CheckEquals(3, DishBase.Version);

  Dish := TDish.Create;
  Dish.Name := '�������';
  DishBase.Add(Dish);
  CheckEquals(4, DishBase.Version);

  CheckEquals(4, DishBase.Count, 'Count check failed');

  DishBase.Delete(0);
  CheckEquals(5, DishBase.Version);

  CheckEquals(3, DishBase.Count, 'Count check failed');

  CheckEqualsString('��������', DishBase[0].Name);
  CheckEqualsString('�������� �����', DishBase[1].Name);
  CheckEqualsString('�������', DishBase[2].Name);

  DishBase.Free;
end;


initialization
  TestFramework.RegisterTest(TFoodBaseTest.Suite);
  TestFramework.RegisterTest(TDishBaseTest.Suite);
end.
