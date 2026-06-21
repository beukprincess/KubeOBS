from pydantic import BaseModel, EmailStr, Field

#
class ClusterCreate(BaseModel):
    name: str
    endpoint_url: str
    cluster_token: str
    
class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=8, max_length=128, pattern=r'(?=.*[!@#$%^&*()_+\-=\[\]{};\':"\\|,.<>\/?])',description="Password must contain at least one special character and contain more than 8 characters.")

class UserResponse(BaseModel):
    id: int
    email: EmailStr
    is_active: bool

    class Config:
        from_attributes = True
class UserLogin(BaseModel):
    email: str
    password: str

class UserInfo(BaseModel):
    id: int
    email: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    user: UserInfo

class ClusterResponse(BaseModel):
    id: int
    name: str
    endpoint_url: str
    user_id: int

    class Config:
        from_attributes = True