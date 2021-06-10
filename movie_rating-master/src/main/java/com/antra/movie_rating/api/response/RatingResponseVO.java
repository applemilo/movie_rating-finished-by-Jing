package com.antra.movie_rating.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponseVO {
	private String msg;
	private Float rating;
	private int movieId;
	private boolean enableRate;
	private int userId;
}
