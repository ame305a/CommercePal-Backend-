package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceAlreadyExistsException;
import com.commerce.pal.backend.common.exceptions.customExceptions.ResourceNotFoundException;
import com.commerce.pal.backend.dto.user.UserInvitationResponse;
import com.commerce.pal.backend.dto.user.UserInvitationSendReq;
import com.commerce.pal.backend.models.LoginValidation;
import com.commerce.pal.backend.models.user.invitation.UserInvitation;
import com.commerce.pal.backend.repo.LoginValidationRepository;
import com.commerce.pal.backend.repo.user.invitation.UserInvitationRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInvitationService {

    private final LoginValidationRepository loginValidationRepository;
    private final GlobalMethods globalMethods;
    private final CustomerService customerService;
    private final UserInvitationRepository userInvitationRepository;

    public List<UserInvitationResponse> checkPhoneNumbers(Long userId, Set<String> phoneNumbers) {
        // Clean phone numbers. there can be spaces in the phone number
        // Don't use parallel stream for this case.
        phoneNumbers = phoneNumbers.stream()
                .map(phoneNumber -> phoneNumber.replace(" ", ""))
                .collect(Collectors.toSet());

        ConcurrentHashMap<String, Boolean> phoneNumberExistsMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Boolean> invitationExistsMap = new ConcurrentHashMap<>();

        phoneNumbers.parallelStream()
                .filter(phoneNumber -> phoneNumber.length() >= 9)
                .forEach(phoneNumber -> {
                    String formatPhoneNumberToEthiopianStandard = GlobalMethods.formatPhoneNumberToEthiopianStandard(phoneNumber);
                    phoneNumberExistsMap.put(phoneNumber, loginValidationRepository.existsByPhoneNumber(formatPhoneNumberToEthiopianStandard));
                    Optional<UserInvitation> userInvitation = userInvitationRepository.findByPhoneNumber(formatPhoneNumberToEthiopianStandard);
                    if (userInvitation.isPresent()) {
                        invitationExistsMap.put(phoneNumber, userInvitation.get().getInvitedBy().contains(userId));
                    } else {
                        invitationExistsMap.put(phoneNumber, false);
                    }
                });

        List<UserInvitationResponse> result = new ArrayList<>();
        for (String phoneNumber : phoneNumbers) {
            Boolean phoneExists = phoneNumberExistsMap.getOrDefault(phoneNumber, Boolean.FALSE);
            Boolean invitationExists = invitationExistsMap.getOrDefault(phoneNumber, Boolean.FALSE);
            result.add(new UserInvitationResponse(phoneNumber, phoneExists, invitationExists));
        }
        return result;
    }

    @Async
    public void sendInvitation(LoginValidation user, String formattedPhoneNumber, String appLink) {
        JSONObject customer = customerService.getMultiUserCustomer(user.getEmailAddress(), user.getPhoneNumber());
        String inviter = String.format("%s %s", customer.getString("firstName"), customer.getString("lastName"));

        JSONObject smsBody = new JSONObject();
        smsBody.put("TemplateId", "17");
        smsBody.put("TemplateLanguage", "en");
        smsBody.put("Phone", formattedPhoneNumber); //invitee phone number
        smsBody.put("inviter", inviter);
        smsBody.put("appLink", appLink);

        globalMethods.sendSMSNotification(smsBody);

        saveUserInvitation(user.getLoginId(), formattedPhoneNumber);
    }


    private void saveUserInvitation(Long userId, String phoneNumber) {
        UserInvitation userInvitation = userInvitationRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    UserInvitation invitation = new UserInvitation();
                    invitation.setPhoneNumber(phoneNumber);
                    return invitation;
                });

        userInvitation.addInvitedBy(userId);
        userInvitationRepository.save(userInvitation);
    }

}

