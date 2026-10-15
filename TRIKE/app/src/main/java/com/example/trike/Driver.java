package com.example.trike;

public class Driver {

    private String id;
    private String name;
    private String plateNumber;
    private String terminal;
    private String imageUrl;
    private String licenseUrl; // ⭐ ADDED

    // Empty constructor required for Firebase
    public Driver() {
    }

    // FULL constructor (used in AddDriverActivity)
    public Driver(String id, String name, String plateNumber, String terminal,
                  String imageUrl, String licenseUrl) {

        this.id = id;
        this.name = name;
        this.plateNumber = plateNumber;
        this.terminal = terminal;
        this.imageUrl = imageUrl;
        this.licenseUrl = licenseUrl; // ⭐ ADDED
    }

    // GETTERS & SETTERS

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // ⭐ ADDED — LICENSE IMAGE URL SUPPORT
    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }
}
