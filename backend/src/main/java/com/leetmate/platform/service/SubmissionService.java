package com.leetmate.platform.service;

import com.leetmate.platform.ai.AiReviewProvider;
import com.leetmate.platform.ai.AiReviewResult;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.submission.ReviewResponse;
import com.leetmate.platform.dto.submission.SubmissionResponse;
import com.leetmate.platform.dto.submission.SubmitSolutionRequest;
import com.leetmate.platform.entity.Submission;
import com.leetmate.platform.entity.SubmissionReview;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.SubmissionRepository;
import com.leetmate.platform.util.CyclomaticComplexityCalculator;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Coordinates submission persistence, AI reviews and complexity calculation.
 */
@Service
public class SubmissionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SubmissionRepository submissionRepository;
    private final ChallengeService challengeService;
    private final AiReviewProvider aiReviewProvider;
    private final CyclomaticComplexityCalculator complexityCalculator;

    /**
     * Creates the service.
     *
     * @param submissionRepository repository
     * @param challengeService     challenge service
     * @param aiReviewProvider     AI provider
     * @param complexityCalculator complexity calculator
     */
    public SubmissionService(SubmissionRepository submissionRepository,
                             ChallengeService challengeService,
                             AiReviewProvider aiReviewProvider,
                             CyclomaticComplexityCalculator complexityCalculator) {
        this.submissionRepository = submissionRepository;
        this.challengeService = challengeService;
        this.aiReviewProvider = aiReviewProvider;
        this.complexityCalculator = complexityCalculator;
    }

    /**
     * Handles a submission end-to-end: persistence, AI review and credit award.
     *
     * @param challengeId challenge identifier
     * @param request     payload
     * @return submission response
     */
    public SubmissionResponse submit(UUID challengeId, SubmitSolutionRequest request) {
        challengeService.findChallenge(challengeId);
        Submission submission = new Submission(UUID.randomUUID(), challengeId,
                request.getLanguage(), request.getCode(), 1, Instant.now());
        AiReviewResult reviewResult = aiReviewProvider.review(request.getLanguage(), request.getCode());
        int complexity = complexityCalculator.calculate(request.getCode());
        SubmissionReview review = new SubmissionReview(UUID.randomUUID(),
                reviewResult.getCreatedAt() == null ? Instant.now() : reviewResult.getCreatedAt(),
                reviewResult.getSummary(), complexity, reviewResult.getSuggestions());
        submission.attachReview(review);
        submissionRepository.save(submission);
        return toResponse(submission);
    }

    /**
     * Retrieves a submission by identifier.
     *
     * @param submissionId identifier
     * @return response
     */
    public SubmissionResponse getSubmission(UUID submissionId) {
        return toResponse(findSubmission(submissionId));
    }

    /**
     * Lists submissions for a challenge.
     *
     * @param challengeId challenge identifier
     * @param page        requested page
     * @param size        requested size
     * @return paginated response
     */
    public PageResponse<SubmissionResponse> listSubmissions(UUID challengeId, int page, int size) {
        challengeService.findChallenge(challengeId);
        validatePagination(page, size);
        List<Submission> submissions = submissionRepository.findByChallengeId(challengeId);
        int fromIndex = Math.min(page * size, submissions.size());
        int toIndex = Math.min(fromIndex + size, submissions.size());
        List<SubmissionResponse> content = submissions.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();
        int totalPages = (int) Math.ceil((double) submissions.size() / size);
        return new PageResponse<>(content, page, size, submissions.size(), totalPages);
    }

    private Submission findSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission %s not found".formatted(submissionId)));
    }

    private SubmissionResponse toResponse(Submission submission) {
        ReviewResponse reviewResponse = submission.getReview()
                .map(review -> new ReviewResponse(review.getId(), review.getSummary(),
                        review.getComplexity(), review.getSuggestions(), review.getCreatedAt()))
                .orElse(null);
        return new SubmissionResponse(submission.getId(), submission.getChallengeId(),
                submission.getLanguage(), submission.getCode(), submission.getCreditsAwarded(),
                submission.getCreatedAt(), reviewResponse);
    }

    private void validatePagination(int page, int size) {
        Assert.isTrue(page >= 0, "page must be greater or equal to 0");
        Assert.isTrue(size > 0 && size <= MAX_PAGE_SIZE,
                "size must be between 1 and " + MAX_PAGE_SIZE);
    }
}
