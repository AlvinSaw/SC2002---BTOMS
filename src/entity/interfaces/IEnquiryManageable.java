package entity.interfaces;

import entity.Enquiry;
import java.util.List;

public interface IEnquiryManageable {
    List<Enquiry> getEnquiries();
    boolean canEditEnquiry(Enquiry enquiry);
    boolean canDeleteEnquiry(Enquiry enquiry);
} 