package shared.snapshot;

// record works with Jackson 2.12+; if older, switch to a POJO with public field.
public record StartGameDTO(int level) {
    // Jackson-friendly: nothing else needed for records
}
