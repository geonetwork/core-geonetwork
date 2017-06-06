package org.fao.geonet.api.userfeedback;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.modelmapper.ModelMapper;

public class UserFeedbackUtils {

    public RatingAverage getAverage(List<UserFeedback> list) {
        int avg = 0;
        int countAvg = 0;
        for (UserFeedback userFeedback : list) {

            if(userFeedback.getDetailedRatingList()!=null && userFeedback.getDetailedRatingList().size()>0) {
                for (Rating rating : userFeedback.getDetailedRatingList()) {
                    if(rating.getCategory().equals(Rating.Category.AVG)) {
                        countAvg++;
                        avg = avg + rating.getRating();
                    }
                }

            }
        }
        avg = avg/countAvg;

        RatingAverage v = new RatingAverage(avg, list.size());

        return v;
    }



    public class RatingAverage {
        private int ratingAverage;
        private int userfeedbackCount;

        public RatingAverage(int ratingAverage, int userfeedbackCount) {
            this.ratingAverage = ratingAverage;
            this.userfeedbackCount = userfeedbackCount;
        }

        public int getRatingAverage() {
            return ratingAverage;
        }
        public void setRatingAverage(int ratingAverage) {
            this.ratingAverage = ratingAverage;
        }
        public int getUserfeedbackCount() {
            return userfeedbackCount;
        }
        public void setUserfeedbackCount(int userfeedbackCount) {
            this.userfeedbackCount = userfeedbackCount;
        }


    }

    public static UserFeedbackDTO convertToDto(UserFeedback userfeedback) {

        ModelMapper modelMapper = new ModelMapper();

        UserFeedbackDTO userfeedbackDto = modelMapper.map(userfeedback, UserFeedbackDTO.class);

        List<Rating> ratingList = userfeedback.getDetailedRatingList();

        for (Rating rating : ratingList) {
            if(rating.getCategory().equals(Rating.Category.AVG)) {
                userfeedbackDto.setRatingAVG(rating.getRating());
            }
            if(rating.getCategory().equals(Rating.Category.COMPLETE)) {
                userfeedbackDto.setRatingCOMPLETE(rating.getRating());
            }
            if(rating.getCategory().equals(Rating.Category.FINDABILITY)) {
                userfeedbackDto.setRatingFINDABILITY(rating.getRating());
            }
            if(rating.getCategory().equals(Rating.Category.READABILITY)) {
                userfeedbackDto.setRatingREADABILITY(rating.getRating());
            }
            if(rating.getCategory().equals(Rating.Category.OTHER)) {
                userfeedbackDto.setRatingOTHER(rating.getRating());
            }
        }

        return userfeedbackDto;
    }

    public static UserFeedback convertFromDto(UserFeedbackDTO userfeedbackDto, User author) {

        ModelMapper modelMapper = new ModelMapper();

        UserFeedback userfeedback = modelMapper.map(userfeedbackDto, UserFeedback.class);

        List<Rating> ratingList = null;

        userfeedback.setDetailedRatingList(ratingList = new ArrayList<Rating>());

        if(userfeedbackDto.getRatingAVG()!=0) {

            Rating r = new Rating();

            r.setRating(userfeedbackDto.getRatingAVG());
            r.setCategory(Rating.Category.AVG);

            ratingList.add(r);
        }
        if(userfeedbackDto.getRatingCOMPLETE()!=0) {

            Rating r = new Rating();

            r.setRating(userfeedbackDto.getRatingCOMPLETE());
            r.setCategory(Rating.Category.COMPLETE);

            ratingList.add(r);
        }
        if(userfeedbackDto.getRatingFINDABILITY()!=0) {

            Rating r = new Rating();

            r.setRating(userfeedbackDto.getRatingFINDABILITY());
            r.setCategory(Rating.Category.FINDABILITY);

            ratingList.add(r);
        }
        if(userfeedbackDto.getRatingREADABILITY()!=0) {

            Rating r = new Rating();

            r.setRating(userfeedbackDto.getRatingREADABILITY());
            r.setCategory(Rating.Category.READABILITY);

            ratingList.add(r);
        }
        if(userfeedbackDto.getRatingOTHER()!=0) {

            Rating r = new Rating();

            r.setRating(userfeedbackDto.getRatingOTHER());
            r.setCategory(Rating.Category.OTHER);

            ratingList.add(r);
        }
        
        if(author!=null) {
            userfeedback.setAuthor(author);
            userfeedback.setStatus(UserRatingStatus.PUBLISHED);
        } else {
            userfeedback.setAuthor(null);
            userfeedback.setStatus(UserRatingStatus.WAITING_FOR_APPROVAL);
        }
        
        userfeedback.setApprover(null);
               
        return userfeedback;
    }

}
