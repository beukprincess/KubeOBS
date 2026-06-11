import datetime
from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, Float
from database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(150), unique=True, nullable=False, index=True)
    hashed_password = Column(String(255), nullable=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    is_active = Column(Boolean, default=True)

class Node(Base):
    __tablename__ = "nodes"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    status = Column(String(50))
    cpu_cores = Column(Integer)
    memory_bytes = Column(String(50))
    cluster_id = Column(Integer, ForeignKey("clusters.id", ondelete="CASCADE"))

class Pod(Base):
    __tablename__ = "pods"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(150), nullable=False)
    namespace = Column(String(100), nullable=False)
    status = Column(String(50))
    cluster_id = Column(Integer, ForeignKey("clusters.id", ondelete="CASCADE"))

class SystemMetric(Base):
    __tablename__ = "metrics"

    id = Column(Integer, primary_key=True, index=True)
    cpu_utilization = Column(Float, nullable=False)
    memory_utilization = Column(Float, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow, index=True)
    cluster_id = Column(Integer, ForeignKey("clusters.id", ondelete="CASCADE"))

class Cluster(Base):
    __tablename__ = "clusters"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False, unique=True)
    endpoint_url = Column(String(255), nullable=False)
    cluster_token = Column(String(1000), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"))