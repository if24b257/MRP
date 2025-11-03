package org.SalimMRP.business;

import org.SalimMRP.business.dto.LeaderboardEntry;
import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.UserProfile;
import org.SalimMRP.persistence.models.Rating;

import java.util.List;

// Liefert zusammengesetzte Informationen rund um Benutzerprofile.
public interface ProfileService {

    UserProfile buildProfile(int userId);

    List<Rating> ratingHistory(int userId);

    List<MediaDetails> favoriteMedia(int userId);

    List<LeaderboardEntry> leaderboard(int limit);
}
