package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.OrderItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;

    @Async
    @SneakyThrows
    public void sendOrderConfirmationEmail(String toEmail, String subject, List<OrderItem> orderItems) {
        Order order = orderItems.get(0).getOrder();

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);

        // Load the Freemarker template
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates"); // Set the path to your templates
        Template template = freemarkerConfig.getTemplate("order-confirmation-email-template.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("subject", subject);
        model.put("items", orderItems);
        model.put("order", order);

        // Generate email content from the template
        String emailContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }
}