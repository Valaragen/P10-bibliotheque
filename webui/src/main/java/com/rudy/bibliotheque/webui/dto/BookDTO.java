package com.rudy.bibliotheque.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rudy.bibliotheque.webui.util.Constant;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {
    private Long id;

    @NotNull
    @Pattern(regexp = "^(\\d{10}|\\d{13})$", message = Constant.MSG_WRONG_ISBN_REGEX)
    private String isbn;

    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    @Size(min = 1, max = 100)
    private String author;

    @NotNull
    @Size(min = 1, max = 100)
    private String publisher;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date releaseDate;

    private Integer copyNumber;

    private Integer availableCopyNumber;

    private List<ReservationDTO> ongoingReservations;

    private List<CopyDTO> copies;
}
