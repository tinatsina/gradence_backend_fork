package com.gradence.ga.controller;

import com.gradence.ga.model.Exam;
import com.gradence.ga.model.Question;
import com.gradence.ga.repository.ExamRepository;
import jakarta.validation.Valid;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/exams")
@Validated

public class ExamController {

    private final ExamRepository examRepository;

    public ExamController(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    // Create a new exam here
    @PostMapping
    public Exam createExam(@Valid @RequestBody Exam exam) {
              exam.setCreatedAt(LocalDateTime.now());
              exam.getQuestions().forEach(q -> q.setExam(exam)); // set back-reference
        return examRepository.save(exam);
    }

    // Get all exams
    @GetMapping
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    // Get one exam by ID
    @GetMapping("/{id}")
    public Exam getExam(@PathVariable Long id) {
        return examRepository.findById(id).orElseThrow(() -> new RuntimeException("Exam not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam updatedExam) {
        return examRepository.findById(id).map(existingExam -> {
            existingExam.setTitle(updatedExam.getTitle());
            existingExam.setCreatedAt(LocalDateTime.now());

            // Clear old questions and replace them with new ones
            existingExam.getQuestions().clear();

            updatedExam.getQuestions().forEach(q -> {
                q.setExam(existingExam); // link question to parent exam
                existingExam.getQuestions().add(q);
            });

            Exam saved = examRepository.save(existingExam);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/random")
    public ResponseEntity<List<Question>> getRandomQuestions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int count,
            @RequestParam(required = false) Long seed,
            @RequestParam(required = false) String type) {

        return examRepository.findById(id).map(exam -> {
            // Clone the list to avoid modifying the entity
            List<Question> questions = new ArrayList<>(exam.getQuestions());

            // Filter by type if provided
            if (type != null && !type.isBlank()) {
                questions = questions.stream()
                        .filter(q -> q.getType().equalsIgnoreCase(type))
                        .collect(Collectors.toList());
            }

            if (questions.size() <= count) {
                return ResponseEntity.ok(questions);
            }

            if (seed != null) {
                Collections.shuffle(questions, new Random(seed));
            } else {
                Collections.shuffle(questions);
            }

            List<Question> randomSubset = questions.subList(0, count);
            return ResponseEntity.ok(randomSubset);
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        return examRepository.findById(id)
                .map(exam -> {
                    examRepository.delete(exam);
                    return ResponseEntity.noContent().<Void>build(); //  specify Void here
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportExamAsPdf(@PathVariable Long id) {
        return examRepository.findById(id).map(exam -> {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Exam: " + exam.getTitle());
                contentStream.newLineAtOffset(0, -30);

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (Question q : exam.getQuestions()) {
                    contentStream.showText("Q: " + q.getPrompt());
                    contentStream.newLineAtOffset(0, -20);
                    if (q.getConditions() != null && !q.getConditions().isBlank()) {
                        contentStream.showText("   Conditions: " + q.getConditions());
                        contentStream.newLineAtOffset(0, -20);
                    }
                }

                contentStream.endText();
                contentStream.close();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "exam_" + id + ".pdf");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(out.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body((byte[]) null);
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

}