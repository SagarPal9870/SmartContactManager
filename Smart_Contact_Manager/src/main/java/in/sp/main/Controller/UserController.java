package in.sp.main.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.sp.main.Entity.Contact;
import in.sp.main.Entity.User;
import in.sp.main.Helper.Message;
import in.sp.main.Repository.ContactRepository;
import in.sp.main.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//---------method for adding  common data  to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String username=principal.getName();
		System.out.println("USERNAME:" +username);
		//--------get the user using userName(email)
		User user=this.userRepository.getUserByUserName(username);
		System.out.println("USER "+ user);
		
		model.addAttribute("user", user);
	}
	
	//-------DashBoard home----
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal){	//By the use  of principal we can identify the username.

		String username=principal.getName();
		System.out.println("USERNAME:" +username);
		//--------get the user using userName(email)
		User user=this.userRepository.getUserByUserName(username);
		System.out.println("USER "+ user);
		
		model.addAttribute("user", user);
		model.addAttribute("title", "User DashBoard");
		return "Normal/user_dashboard";
	}
	
	
	
	//Open add_contact from handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "Normal/add_contact_form";
	}
	
	//Processing add contact Form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,  Principal principal) {
		try {
			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			//processing and uploading file..........
			if(file.isEmpty()) {
				//if the file is empty then try our message
				contact.setImage("contact.png");
				System.out.println("File is empty");
				
			}
			else {
				//upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				
				File saveFile=new ClassPathResource("static/Image").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			
			user.getContact().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			
			System.out.println("DATA" +contact);
			System.out.println("Added to data base");
			
			//----------message success..................................
			/*
			 * session.setAttribute("message", new
			 * Message("Your contact is added!! Add more..", "success"));
			 */
		}
		catch(Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			
			//----------error message........................
			/*
			 * session.setAttribute("message", new
			 * Message("Something went wrong !! Try again", "danger"));
			 */
		}
		
		return "Normal/add_contact_form";
	}
	
	
	//------------Show contacts handler
	//-----------Per page =5[n]
	//--------current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m,Principal principal) {
		m.addAttribute("title", "Show user contacts");
		
		//contact ki list ko send karni hai
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable =PageRequest.of(page, 5);
		
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "Normal/show_contacts";
	}
	
	
	
	//---------Showing particular contact details..
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("CID" +cId);
		
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "Normal/contact_detail";
	}
	
	//----Delete contact Handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,
			HttpSession session,Principal principal)
	{
		System.out.println("cId:"+cId);
		Contact contact=this.contactRepository.findById(cId).get();
		//check....
		System.out.println("contact"+contact.getcId());
		contact.setUser(null);
		//remove 
		//img
		//contact.image
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContact().remove(contact);
		this.userRepository.save(user);
		/* this.contactRepository.delete(contact); */
		
		System.out.println("DELETED");
		session.setAttribute("message", new Message("contact deleted successfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//--------- Open Update-form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title", "Update contact");
	    Contact contact=	this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
		return "Normal/update_form";
	}
	
	
	//-------update contact handler...........
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,Model m,
			HttpSession session,Principal principal) {
		
		try {
			//old contact details
			Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get();
			
			//image...
			if(!file.isEmpty()) {
				//file work...
				//rewrite...
				//delete old photo 
				File deleteFile=new ClassPathResource("static/Image").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				//-----update new photo	
				File saveFile=new ClassPathResource("static/Image").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated..","success"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("CONTACT NAME:"+contact.getName());
		System.out.println("CONTACT ID:"+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//---------Your profile Handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile page");
		
		return "Normal/profile";
	}
	
	
	//-----------Open Setting Handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "Normal/settings";
	}
	
	
	//---------Change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		System.out.println("old password "+oldPassword);
		System.out.println("old password "+newPassword);
		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed.. ","success"));
		}
		else {
			//error
			session.setAttribute("message", new Message("Wrong old password... ","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
		
	}
	
	
}
