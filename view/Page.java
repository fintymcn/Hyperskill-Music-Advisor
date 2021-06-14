package advisor.view;

public class Page implements View {

    private String message;

    public Page(String message) {
        this.message = message;
    }

    @Override
    public void printPage() {
        System.out.println(message);
    }

    @Override
    public void next() {
        this.message = "No more pages.";
    }

    @Override
    public void prev() {
        this.message = "No more pages";
    }
}
