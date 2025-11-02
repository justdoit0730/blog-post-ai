package org.justdoit.blog.template;

public enum TextVolumeTemplate {
    V1("1", "약 350 ~ 500자"),
    V2("2", "약 500 ~ 800자"),
    V3("3", "800자 이상");

    private final String subject;
    private final String volume;

    TextVolumeTemplate(String subject, String volume) {
        this.subject = subject;
        this.volume = volume;
    }


    public String getSubject() {
        return subject;
    }

    public String getVolume() {
        return volume;
    }

    public static String getVolumeBySubject(String subject) {
        for (TextVolumeTemplate t : values()) {
            if (t.subject.equals(subject)) {
                return t.volume;
            }
        }
        return V1.volume; // 기본값
    }

}
