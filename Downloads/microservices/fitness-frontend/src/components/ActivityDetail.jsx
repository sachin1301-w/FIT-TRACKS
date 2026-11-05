import { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography } from '@mui/material';
import { useParams } from 'react-router-dom';
import { getActivity, getActivityRecommendation } from '../services/api';

const ActivityDetail = () => {
  const { id } = useParams();
  const [activity, setActivity] = useState(null);
  const [recommendation, setRecommendation] = useState(null);

  useEffect(() => {
    let isMounted = true;

    Promise.allSettled([getActivity(id), getActivityRecommendation(id)])
      .then(([activityResult, recommendationResult]) => {
        if (!isMounted) {
          return;
        }

        if (activityResult.status === 'fulfilled') {
          setActivity(activityResult.value.data);
        } else {
          console.error('Failed to load activity:', activityResult.reason);
        }

        if (recommendationResult.status === 'fulfilled') {
          setRecommendation(recommendationResult.value.data);
        } else {
          console.error('Failed to load activity recommendation:', recommendationResult.reason);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [id]);

  return (
    <Box sx={{ p: 2, maxWidth: 800 }}>
      <Card>
        <CardContent>
          <Typography variant="h5">Activity Details</Typography>
          {activity && (
            <>
              <Typography>Type: {activity.type}</Typography>
              <Typography>Duration: {activity.duration} minutes</Typography>
              <Typography>Calories Burned: {activity.caloriesBurned}</Typography>
              <Typography>
                Date: {activity.startTime || activity.createTime || 'Not available'}
              </Typography>
            </>
          )}
        </CardContent>
      </Card>

      {recommendation && (
        <Card sx={{ mt: 2 }}>
          <CardContent>
            <Typography variant="h5">AI Recommendation</Typography>
            <Typography variant="h6">Analysis</Typography>
            <Typography>{recommendation.recommendation}</Typography>
            <Typography>{recommendation.improvements}</Typography>
            <Typography>{recommendation.safety}</Typography>
            {recommendation.suggestions?.map((suggestion) => (
              <Typography key={suggestion}>- {suggestion}</Typography>
            ))}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default ActivityDetail;
