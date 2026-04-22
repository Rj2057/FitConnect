package com.fitconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {
    private Long id;
    private Long userId;
    private String exerciseName;
    private BigDecimal weight;
    private Integer reps;
    private Integer duration;
    private BigDecimal caloriesBurned;
    private LocalDateTime createdAt;

    public static WorkoutResponseBuilder builder() {
        return new WorkoutResponseBuilder();
    }

    public static class WorkoutResponseBuilder {
        private Long id;
        private Long userId;
        private String exerciseName;
        private BigDecimal weight;
        private Integer reps;
        private Integer duration;
        private BigDecimal caloriesBurned;
        private LocalDateTime createdAt;

        WorkoutResponseBuilder() {
        }

        public WorkoutResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public WorkoutResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public WorkoutResponseBuilder exerciseName(String exerciseName) {
            this.exerciseName = exerciseName;
            return this;
        }

        public WorkoutResponseBuilder weight(BigDecimal weight) {
            this.weight = weight;
            return this;
        }

        public WorkoutResponseBuilder reps(Integer reps) {
            this.reps = reps;
            return this;
        }

        public WorkoutResponseBuilder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public WorkoutResponseBuilder caloriesBurned(BigDecimal caloriesBurned) {
            this.caloriesBurned = caloriesBurned;
            return this;
        }

        public WorkoutResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorkoutResponse build() {
            return new WorkoutResponse(this.id, this.userId, this.exerciseName, this.weight, this.reps, this.duration, this.caloriesBurned, this.createdAt);
        }

        public String toString() {
            return "WorkoutResponse.WorkoutResponseBuilder(id=" + this.id + ", userId=" + this.userId + ", exerciseName=" + this.exerciseName + ", weight=" + this.weight + ", reps=" + this.reps + ", duration=" + this.duration + ", caloriesBurned=" + this.caloriesBurned + ", createdAt=" + this.createdAt + ")";
        }
    }
}
