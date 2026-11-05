import { useEffect, useState } from 'react';
import { getActivities } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Grid, Typography } from '@mui/material';

const ActivityList = ({ refreshKey }) => {
    const [activities, setActivities] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        let isMounted = true;

        getActivities()
            .then(({ data }) => {
                if (isMounted) {
                    setActivities(data);
                }
            })
            .catch((error) => {
                console.error('Failed to load activities:', error);
            });

        return () => {
            isMounted = false;
        };
    }, [refreshKey]);
    return (
            <Grid container spacing={2}>
                {activities.map((activity) =>(
                    <Grid item key={activity.id} className="activity-card">
                        <Card
                        onClick={() => navigate(`/activities/${activity.id}`)}
                        sx={{ cursor: 'pointer', height: '100%', minWidth: 220, border: '1px solid #e7eaf0', borderRadius: 3, boxShadow: '0 8px 24px rgba(23, 32, 51, 0.05)', transition: 'transform 220ms ease, box-shadow 220ms ease', '&:hover': { transform: 'translateY(-6px)', boxShadow: '0 18px 34px rgba(37, 99, 235, 0.16)' } }}>
                            <CardContent>
                                
                                <Typography variant='h6' sx={{ color: '#2563eb', fontWeight: 800 }}>{activity.type}</Typography>
                                <Typography>Duration: {activity.duration} minutes</Typography>
                                <Typography>Calories: {activity.caloriesBurned}</Typography>
                            </CardContent>
                        </Card>
                     
                    </Grid>
                ))}
            </Grid>
    )
}
export default ActivityList