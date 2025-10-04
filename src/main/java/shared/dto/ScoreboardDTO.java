// shared/dto/ScoreboardDTO.java
package shared.dto;

public final class ScoreboardDTO {
    public String topUser;         // username
    public int    topXp;           // best XP
    public double bestTimeLevel1;  // example
    public double bestTimeAll;     // example
    public ScoreboardDTO() {}
    public ScoreboardDTO(String topUser, int topXp, double bestTimeLevel1, double bestTimeAll){
        this.topUser = topUser; this.topXp = topXp;
        this.bestTimeLevel1 = bestTimeLevel1; this.bestTimeAll = bestTimeAll;
    }
}
