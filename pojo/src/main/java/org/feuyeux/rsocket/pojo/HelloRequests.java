package org.feuyeux.rsocket.pojo;

import java.util.List;

import lombok.*;

/**
 * @author feuyeux@gmail.com
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HelloRequests {
    private List<String> ids;
}
