package advisor;

import advisor.controller.InputController;

public class Main {
    public static void main(String[] args) {
        InputController controller = new InputController(args);
        controller.run();
    }
}