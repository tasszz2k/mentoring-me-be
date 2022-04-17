package com.labate.mentoringme.service.quizz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import com.labate.mentoringme.dto.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.labate.mentoringme.constant.UserRole;
import com.labate.mentoringme.dto.mapper.PageCriteriaPageableMapper;
import com.labate.mentoringme.dto.request.PageCriteria;
import com.labate.mentoringme.dto.request.quiz.CreateQuizRequest;
import com.labate.mentoringme.dto.request.quiz.FindQuizRequest;
import com.labate.mentoringme.dto.request.quiz.ResultQuizCheckingRequest;
import com.labate.mentoringme.dto.request.quiz.UpdateQuizDetailRequest;
import com.labate.mentoringme.dto.request.quiz.UpdateQuizOverviewRequest;
import com.labate.mentoringme.dto.response.QuizFavoriteResponse;
import com.labate.mentoringme.dto.response.QuizOverviewResponse;
import com.labate.mentoringme.dto.response.QuizResponse;
import com.labate.mentoringme.dto.response.QuizResultResponse;
import com.labate.mentoringme.dto.response.QuizTakingHistoryResponse;
import com.labate.mentoringme.model.quiz.Answer;
import com.labate.mentoringme.model.quiz.FavoriteQuiz;
import com.labate.mentoringme.model.quiz.Question;
import com.labate.mentoringme.model.quiz.Quiz;
import com.labate.mentoringme.model.quiz.QuizResult;
import com.labate.mentoringme.repository.FavoriteQuizRepository;
import com.labate.mentoringme.repository.QuestionRepository;
import com.labate.mentoringme.repository.QuizRepository;
import com.labate.mentoringme.repository.QuizResultRepository;
import com.labate.mentoringme.repository.UserRepository;
import com.labate.mentoringme.util.ObjectMapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

  private final QuizRepository quizRepository;
  private final QuestionRepository questionRepository;
  private final QuizResultRepository quizResultRepository;
  private final FavoriteQuizRepository favoriteQuizRepository;
  private final UserRepository userRepository;
  private final ModelMapper modelMapper = new ModelMapper();

  @Override
  public Page<QuizFavoriteResponse> findAllQuiz(FindQuizRequest request, PageCriteria pageCriteria,
      LocalUser localUser) {
    var pageable = PageCriteriaPageableMapper.toPageable(pageCriteria);
    var response = quizRepository.findAllByConditions(request, pageable).map(quiz -> {
      var quizResponse = modelMapper.map(quiz, QuizFavoriteResponse.class);
      var sortedCategory = quizResponse.getCategories().stream().sorted(Comparator.comparing(CategoryDto::getId)).collect(Collectors.toSet());
      quizResponse.setCategories(sortedCategory);
      return quizResponse;
    });

    if (!Objects.isNull(localUser)) {
      if (localUser.getUser().getRole() == UserRole.ROLE_USER) {
        ArrayList<FavoriteQuiz> favoriteQuizzes =
            (ArrayList<FavoriteQuiz>) favoriteQuizRepository.findAllByUserId(localUser.getUserId());
        if (favoriteQuizzes.size() > 0) {
          Collections.sort(favoriteQuizzes, Comparator.comparing(FavoriteQuiz::getQuizId));
          for (QuizFavoriteResponse quiz : response.getContent()) {
            var isLiked = isLiked(favoriteQuizzes, quiz.getId());
            quiz.setIsLiked(isLiked);
          }
        }
      }
    }
    return response;
  }

  @Transactional
  @Override
  public QuizOverviewResponse getQuizOverview(Long quizId, LocalUser localUser) {
    var quizOpt = quizRepository.findById(quizId);
    var quizOverResponse = ObjectMapperUtils.map(quizOpt.get(), QuizOverviewResponse.class);
    var sortedCategory = quizOverResponse.getCategories().stream().sorted(Comparator.comparing(CategoryDto::getId)).collect(Collectors.toSet());
    quizOverResponse.setCategories(sortedCategory);
    if (!Objects.isNull(localUser)) {
      var userId = localUser.getUserId();
      var favoriteQuiz = favoriteQuizRepository.findByUserIdAndQuizId(userId, quizId);
      if (Objects.isNull(favoriteQuiz)) {
        quizOverResponse.setIsLiked(false);
      } else {
        quizOverResponse.setIsLiked(true);
      }
    }
    var user = userRepository.findById(quizOverResponse.getCreatedBy()).get();
    quizOverResponse.setImageUrl(user.getImageUrl());
    quizOverResponse.setRole(user.getRole());;
    return quizOverResponse;

  }

  @Override
  public QuizDetailDto getQuizDetail(Long quizId) {
    var questions = questionRepository.getByQuizId(quizId).stream().map(item -> {
      var questionDto = modelMapper.map(item, QuestionDto.class);
      return questionDto;
    }).collect(Collectors.toSet());
    var quizDetailDto = new QuizDetailDto(questions);
    return quizDetailDto;
  }

  @Transactional
  @Override
  public void deleteById(Long quizId) {
    quizRepository.deleteById(quizId);
    deleteQuestionByQuizId(quizId);
  }

  public void deleteQuestionByQuizId(Long quizId) {
    var questions = questionRepository.getByQuizId(quizId);
    questionRepository.deleteAll(questions);
  }

  @Transactional
  @Override
  public QuizResponse addQuiz(CreateQuizRequest createQuizRequest, LocalUser localUser) {
    var quiz = modelMapper.map(createQuizRequest, Quiz.class);
    quiz.setCreatedBy(localUser.getUserId());
    quiz.setAuthor(localUser.getUser().getFullName());
    var insertedQuiz = quizRepository.save(quiz);
    var questions = new ArrayList();
    for (QuestionDto questionDto : createQuizRequest.getQuestions()) {
      var question = modelMapper.map(questionDto, Question.class);
      question.setQuizId(insertedQuiz.getId());
      for (Answer answer : question.getAnswers()) {
        answer.setQuestion(question);
      }
      questions.add(question);
    }
    questionRepository.saveAll(questions);
    return modelMapper.map(insertedQuiz, QuizResponse.class);
  }

  @Override
  public QuizDetailDto updateQuizDetail(UpdateQuizDetailRequest updateQuizDetailRequest,
      LocalUser localUser) {
    var quizId = updateQuizDetailRequest.getQuizId();
    deleteQuestionByQuizId(quizId);
    var questions = new ArrayList();
    for (QuestionDto questionDto : updateQuizDetailRequest.getQuestions()) {
      var question = modelMapper.map(questionDto, Question.class);
      for (Answer answer : question.getAnswers()) {
        answer.setQuestion(question);
      }
      questions.add(question);
    }
    var questionDtos = questionRepository.saveAll(questions).stream().map(item -> {
      var questionDto = modelMapper.map(item, QuestionDto.class);
      return questionDto;
    }).collect(Collectors.toSet());

    quizRepository.updateNumberOfQuestion(quizId, questions.size());
    return new QuizDetailDto((Set<QuestionDto>) questionDtos);
  }

  @Override
  public Page<QuizTakingHistoryResponse> getQuizTakingHistory(PageCriteria pageCriteria,
      LocalUser localUser) {
    var pageable = PageCriteriaPageableMapper.toPageable(pageCriteria);
    var userId = localUser.getUserId();
    var response = quizResultRepository.getQuizTakingHistory(userId, pageable).map(item -> {
      var quizTakingHistoryResponse = modelMapper.map(item, QuizTakingHistoryResponse.class);
      return quizTakingHistoryResponse;
    });
    return response;
  }

  @Override
  public QuizResultResponse getQuizResult(ResultQuizCheckingRequest request, LocalUser localUser) {
    var results = questionRepository.getQuizResult(request.getQuizId()).stream().map(ele -> {
      var item = modelMapper.map(ele, QuizResultCheckingDto.class);
      return item;
    }).collect(Collectors.toList());
    var response = calculateUserResult(request, results);
    saveToQuizResult(response, request.getQuizId(), localUser);
    return response;
  }

  private void saveToQuizResult(QuizResultResponse response, Long quizId, LocalUser localUser) {
    if (!Objects.isNull(localUser)) {
      var quizResult = modelMapper.map(response, QuizResult.class);
      quizResult.setQuizId(quizId);
      quizResult.setUserId(localUser.getUserId());
      quizResult.setIsDeleted(false);
      quizResultRepository.save(quizResult);
    }
  }

  private QuizResultResponse calculateUserResult(ResultQuizCheckingRequest request,
      List<QuizResultCheckingDto> results) {
    results.sort(Comparator.comparing(QuizResultCheckingDto::getQuestionId)
        .thenComparing(QuizResultCheckingDto::getAnswerId));

    Map<Long, String> answers = new HashMap();
    int i = 0, numberOfQuestion = 0;
    while (i < results.size()) {
      numberOfQuestion++;
      Long questionId = results.get(i).getQuestionId();
      String answer = results.get(i).getAnswerId() + "-";
      if (i == results.size() - 1) {
        answers.put(questionId, answer);
        break;
      }
      while (i < results.size()) {
        i++;
        if (i == results.size()) {
          answers.put(questionId, answer);
          break;
        }
        if (results.get(i).getQuestionId() == questionId) {
          answer = answer + results.get(i).getAnswerId() + "-";
        } else {
          answers.put(questionId, answer);
          break;
        }
      }
      if (i == results.size())
        break;
    }
    var numberOfFalse = numberOfQuestion - request.getUserSelection().size();
    for (UserSelectionDto userSelection : request.getUserSelection()) {
      var questionId = userSelection.getQuestionId();
      if (userSelection.getAnswerIds() == null) {
        numberOfFalse++;
        continue;
      }
      Collections.sort(userSelection.getAnswerIds());
      String answer = "";
      for (Long answerId : userSelection.getAnswerIds()) {
        if (answer.isEmpty())
          answer = answerId + "-";
        else
          answer = answer + answerId + "-";
      }
      var trueAnswer = answers.get(questionId);
      if (answer.equals(trueAnswer) == false)
        numberOfFalse++;
    }
    var score = 100 - (numberOfFalse * 100 / numberOfQuestion);
    var response = new QuizResultResponse();
    response.setNumberOfFalse(numberOfFalse);
    response.setNumberOfQuestion(numberOfQuestion);
    response.setNumberOfTrue(numberOfQuestion - numberOfFalse);
    response.setScore(score);
    return response;
  }

  @Override
  public Page<QuizResponse> getListDraftQuiz(PageCriteria pageCriteria, LocalUser localUser) {
    var pageable = PageCriteriaPageableMapper.toPageable(pageCriteria);
    var userId = localUser.getUserId();
    return quizRepository.findAllByCreatedByAndIsDraft(userId, true, pageable).map(quiz -> {
      var quizResponse = modelMapper.map(quiz, QuizResponse.class);
      var sortedCategory = quizResponse.getCategories().stream().sorted(Comparator.comparing(CategoryDto::getId)).collect(Collectors.toSet());
      quizResponse.setCategories(sortedCategory);
      return quizResponse;
    });
  }

  @Override
  public QuizResponse updateQuizOverview(UpdateQuizOverviewRequest request, LocalUser localUser) {
    var oldQuiz = quizRepository.findById(request.getId()).get();
    var quiz = modelMapper.map(request, Quiz.class);
    quiz.setModifiedDate(new Date());
    quiz.setModifiedBy(localUser.getUserId());
    quiz.setAuthor(oldQuiz.getAuthor());
    quiz.setCreatedBy(oldQuiz.getCreatedBy());
    return modelMapper.map(quizRepository.save(quiz), QuizResponse.class);
  }

  @Override
  public void publishQuiz(Long quizId) {
    quizRepository.publishQuiz(quizId);
  }

  private Boolean isLiked(ArrayList<FavoriteQuiz> favoriteQuizs, Long quizId) {
    int left = 0;
    int right = favoriteQuizs.size() - 1;
    while (right >= left) {
      int mid = left + (right - left) / 2;
      var id = favoriteQuizs.get(mid).getQuizId();
      if (id == quizId)
        return true;
      if (id > quizId) {
        right = mid - 1;
      } else {
        left = mid + 1;
      }
    }
    return false;
  }

  @Override
  public Optional<Quiz> findById(Long id) {
    var quiz = quizRepository.findById(id);
    if (quiz.isPresent()) {
      return Optional.of(quiz.get());
    }
    return Optional.empty();
  }
}
