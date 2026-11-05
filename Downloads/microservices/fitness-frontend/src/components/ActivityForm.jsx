import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import { useState } from 'react'
import { addActivities } from '../services/api'


const ActivityForm = ({ onActivitiesAdded = () => {} }) => {

    const [activity, setActivity] = useState({
        type: "RUNNING", duration: '', caloriesBurned: '',
        additionalMetrics: {}
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await addActivities(activity);
            onActivitiesAdded();
            setActivity({ type: "RUNNING", duration: '', caloriesBurned: ''});
        } catch (error) {
            console.error(error);
        }
    }
    
  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mb: 5, p: { xs: 2, sm: 3 }, border: '1px solid #e7eaf0', borderRadius: 3, background: '#fff', boxShadow: '0 12px 30px rgba(23, 32, 51, 0.06)' }}>
    <FormControl fullWidth sx={{mb: 2}}>
        <InputLabel>Activity Type</InputLabel>
        <Select
            value={activity.type}
            onChange={(e) => setActivity({...activity, type: e.target.value})}>
                <MenuItem value="RUNNING">Running</MenuItem>
                <MenuItem value="WALKING">Walking</MenuItem>
                <MenuItem value="CYCLING">Cycling</MenuItem>
            </Select>
    </FormControl>
    <TextField fullWidth
                label="Duration (Minutes)"
                type='number'
                sx={{ mb: 2}}
                value={activity.duration}
                onChange={(e) => setActivity({...activity, duration: e.target.value})}/>

<TextField fullWidth
                label="Calories Burned"
                type='number'
                sx={{ mb: 2}}
                value={activity.caloriesBurned}
                onChange={(e) => setActivity({...activity, caloriesBurned: e.target.value})}/>

<Button type='submit' variant='contained' sx={{ borderRadius: 2, px: 3, py: 1.2, textTransform: 'none', fontWeight: 700 }}>
    Add Activity
</Button>
  </Box>
  )
}

export default ActivityForm