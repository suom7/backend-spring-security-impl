
package com.backend.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Profile implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            profileId;
    private String            cardHolderName;
    private String            email;
    private String            cardNumber;
    private String            status;
    private String            photo;
}
