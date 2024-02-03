package dev.isxander.controlify.driver;

public interface NameProviderDriver extends Driver {
    NameProviderDriver UNSUPPORTED = new NameProviderDriver() {
        @Override
        public void update() {

        }

        @Override
        public String getName() {
            return "Unknown Controller";
        }

        @Override
        public String getNameProviderDetails() {
            return "Unsupported";
        }
    };

    String getName();

    String getNameProviderDetails();
}
