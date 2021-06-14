package advisor.view;

public class NumberedPage implements View{

    private int totalPages;
    private int currentPage;
    private int itemsPerPage;
    private String[] items;
    private boolean lastPageOdd;
    private boolean pageOutOfBounds;

    String footer = "---PAGE %d OF %d---";

    public NumberedPage(int itemsPerPage, String[] items) {
        this.itemsPerPage = itemsPerPage;
        this.items = items;
        this.currentPage = 1;
        this.totalPages = Math.max(items.length / itemsPerPage, 1);
        this.lastPageOdd = (items.length % itemsPerPage != 0);
        this.pageOutOfBounds = false;

        if (lastPageOdd && totalPages > 1) {
            totalPages++;
        }
    }

    @Override
    public void printPage() {
        if (pageOutOfBounds) {
            System.out.println("No more pages");
            return;
        }

        int currentIndex = (currentPage - 1) * itemsPerPage;

        if (currentPage == totalPages && lastPageOdd) {
            int remaining = items.length % itemsPerPage;
            for (int j = 0; j < remaining; j++) {
                System.out.println(items[currentIndex]);
                currentIndex++;
            }
        } else {
            for (int j = 0; j < itemsPerPage; j++) {
                System.out.println(items[currentIndex]);
                currentIndex++;
            }
        }
        System.out.println(String.format(footer, currentPage, totalPages));
    }

    @Override
    public void next() {
        if (currentPage == totalPages) {
            pageOutOfBounds = true;
        } else {
            pageOutOfBounds = false;
            currentPage++;
        }
    }

    @Override
    public void prev() {
        if (currentPage == 1) {
            pageOutOfBounds = true;
        } else {
            pageOutOfBounds = false;
            currentPage--;
        }
    }
}
