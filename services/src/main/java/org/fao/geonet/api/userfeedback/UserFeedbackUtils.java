package org.fao.geonet.api.userfeedback;

import java.util.List;

import org.fao.geonet.domain.userfeedback.UserFeedback;

public class UserFeedbackUtils {
    
    public RatingAverage getAverage(List<UserFeedback> list) {
        int avg = 0;
        int countAvg = 0;
        for (UserFeedback userFeedback : list) {
            if(userFeedback.getRating()!=null && userFeedback.getRating().getRating()>0) {
                countAvg++;
                avg = avg + userFeedback.getRating().getRating();
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

}
