package com.giovds.poc.evaluator;

import com.giovds.poc.github.model.internal.Collected;
import com.giovds.poc.github.model.internal.RangeSummary;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MaintenanceEvaluator {
    public double evaluateCommitsFrequency(Collected collected) {
        var commits = collected.getCommits();
        if (commits.isEmpty()) {
            return 0;
        }

        var range30 = findRange(commits, 30);
        var range180 = findRange(commits, 180);
        var range365 = findRange(commits, 365);

        var mean30 = range30.size();
        var mean180 = range180.size() / (180.0d / 30.0d);
        var mean365 = range365.size() / (365.0d / 30.0d);

        var monthlyMean = (mean30 * 0.35d) +
                (mean180 * 0.45d) +
                (mean365 * 0.2d);

        return normalizeValue(monthlyMean, List.of(
                new NormalizeStep(0d, 0d),
                new NormalizeStep(1d, 0.7d),
                new NormalizeStep(5d, 0.9d),
                new NormalizeStep(10d, 1d)
        ));
    }

    private List<RangeSummary> findRange(List<RangeSummary> commits, int days) {
        return commits.stream()
                .filter(range -> Duration.between(range.getStart(), range.getEnd()).toDays() == days)
                .collect(Collectors.toList());
    }

    private double normalizeValue(double value, List<NormalizeStep> steps) {
        var index = findLastIndex(steps, step -> value <= step.value());

        if (index == -1) {
            return steps.getFirst().norm();
        }
        if (index == steps.size() - 1) {
            return steps.getLast().norm();
        }

        var stepLow = steps.get(index);
        var stepHigh = steps.get(index + 1);

        return stepLow.norm() + ((stepHigh.norm - stepLow.norm) * (value - stepLow.value)) / (stepHigh.value - stepLow.value);
    }

    public static <T> int findLastIndex(List<T> list, Predicate<T> predicate) {
        var reverseIdx = IntStream.range(0, list.size())
                .filter(i -> predicate.test(list.get(i)))
                .findFirst()
                .orElse(-1);

        return reverseIdx == -1 ? reverseIdx : list.size() - (reverseIdx + 1);
    }

    private record NormalizeStep(double value, double norm) {
    }
}
