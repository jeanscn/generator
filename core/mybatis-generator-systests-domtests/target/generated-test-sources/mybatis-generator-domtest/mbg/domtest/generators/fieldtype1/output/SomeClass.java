package mbg.domtest.generators.fieldtype1.output;

public class SomeClass {
    private FieldType main;

    private mbg.domtest.generators.fieldtype1.output.sub1.SomeClass tcSub1;

    private mbg.domtest.generators.fieldtype1.output.sub1.FieldType sub1;

    private mbg.domtest.generators.fieldtype1.output.sub2.FieldType sub2;

    public void executeMain() {
        main.mainMethod();
    }

    public void setMain(FieldType main) {
        this.main = main;
    }

    public FieldType getMain() {
        return main;
    }

    public void executeSub1() {
        sub1.sub1Method();
    }

    public void setSub1(mbg.domtest.generators.fieldtype1.output.sub1.FieldType sub1) {
        this.sub1 = sub1;
    }

    public mbg.domtest.generators.fieldtype1.output.sub1.FieldType getSub1() {
        return sub1;
    }

    public void executeSub2() {
        sub2.sub2Method();
    }

    public void setSub2(mbg.domtest.generators.fieldtype1.output.sub2.FieldType sub2) {
        this.sub2 = sub2;
    }

    public mbg.domtest.generators.fieldtype1.output.sub2.FieldType getSub2() {
        return sub2;
    }
}